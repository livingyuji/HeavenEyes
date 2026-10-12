package dev.livingyuji.heaveneyes.evidence;

import dev.livingyuji.heaveneyes.HeavenEyesPlugin;
import dev.livingyuji.heaveneyes.model.Incident;
import dev.livingyuji.heaveneyes.model.Violation;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Manages incident evidence creation, caching, and persistent serialization.
 */
public class EvidenceManager {

    private final HeavenEyesPlugin plugin;
    private final File incidentsFolder;
    private final Map<String, Incident> incidentCache = new ConcurrentHashMap<>();
    private final Queue<String> incidentOrder = new ConcurrentLinkedQueue<>();
    private final int maxCacheSize;

    private static final String ID_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public EvidenceManager(HeavenEyesPlugin plugin) {
        this.plugin = plugin;
        this.incidentsFolder = new File(plugin.getDataFolder(), "evidence/incidents");
        this.maxCacheSize = plugin.getPluginConfig().getMaxInMemoryIncidents();
    }

    public void initialize() {
        if (!incidentsFolder.exists()) {
            incidentsFolder.mkdirs();
        }
    }

    public String generateIncidentId() {
        StringBuilder sb = new StringBuilder("INC-");
        for (int i = 0; i < 6; i++) {
            sb.append(ID_CHARS.charAt(RANDOM.nextInt(ID_CHARS.length())));
        }
        return sb.toString();
    }

    public String saveIncident(Violation violation, String actionTriggered, String banId) {
        String incidentId = generateIncidentId();
        Incident incident = new Incident(
                incidentId,
                violation.getPlayerUuid(),
                violation.getPlayerName(),
                violation.getCheckType().getIdentifier(),
                violation.getViolationLevel(),
                violation.getConfidence(),
                violation.getTimestamp(),
                violation.getWorld(),
                violation.getX(),
                violation.getY(),
                violation.getZ(),
                violation.getExplanation(),
                violation.getDiagnosticData(),
                actionTriggered,
                banId
        );

        // Put in memory cache
        incidentCache.put(incidentId, incident);
        incidentOrder.offer(incidentId);
        if (incidentCache.size() > maxCacheSize) {
            String oldest = incidentOrder.poll();
            if (oldest != null) {
                incidentCache.remove(oldest);
            }
        }

        // Save asynchronously to disk
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            File file = new File(incidentsFolder, incidentId + ".yml");
            YamlConfiguration config = new YamlConfiguration();
            for (Map.Entry<String, Object> entry : incident.toMap().entrySet()) {
                config.set(entry.getKey(), entry.getValue());
            }
            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save incident " + incidentId + ": " + e.getMessage());
            }

            // Also record in player's evidence index
            plugin.getPlayerDataManager().recordPlayerEvidence(violation.getPlayerUuid(), incidentId, incident.getCheckName());
        });

        return incidentId;
    }

    public Incident getIncident(String incidentId) {
        Incident cached = incidentCache.get(incidentId);
        if (cached != null) return cached;

        File file = new File(incidentsFolder, incidentId + ".yml");
        if (!file.exists()) return null;

        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            Incident incident = Incident.fromMap(config.getValues(false));
            incidentCache.put(incidentId, incident);
            return incident;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load incident " + incidentId + ": " + e.getMessage());
            return null;
        }
    }

    public int getCachedIncidentsCount() {
        return incidentCache.size();
    }
}
