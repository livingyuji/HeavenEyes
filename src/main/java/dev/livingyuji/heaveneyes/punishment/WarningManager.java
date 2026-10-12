package dev.livingyuji.heaveneyes.punishment;

import dev.livingyuji.heaveneyes.HeavenEyesPlugin;
import dev.livingyuji.heaveneyes.model.CheckType;
import dev.livingyuji.heaveneyes.model.WarningRecord;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages warning progression, expiration, and persistence per player UUID.
 */
public class WarningManager {

    private final HeavenEyesPlugin plugin;
    private final Map<UUID, List<WarningRecord>> playerWarnings = new ConcurrentHashMap<>();

    public WarningManager(HeavenEyesPlugin plugin) {
        this.plugin = plugin;
    }

    public synchronized List<WarningRecord> getActiveWarnings(UUID uuid) {
        List<WarningRecord> list = playerWarnings.computeIfAbsent(uuid, this::loadWarningsFromDisk);
        long resetPeriod = plugin.getPunishmentsConfig().getWarningResetMillis();
        long now = System.currentTimeMillis();

        // Expire old warnings
        list.removeIf(w -> (now - w.getTimestamp()) > resetPeriod);
        return Collections.unmodifiableList(new ArrayList<>(list));
    }

    public int getActiveWarningCount(UUID uuid) {
        return getActiveWarnings(uuid).size();
    }

    public synchronized WarningRecord addWarning(UUID uuid, CheckType checkType, String evidenceId, String reason) {
        List<WarningRecord> list = playerWarnings.computeIfAbsent(uuid, this::loadWarningsFromDisk);
        long resetPeriod = plugin.getPunishmentsConfig().getWarningResetMillis();
        long now = System.currentTimeMillis();

        list.removeIf(w -> (now - w.getTimestamp()) > resetPeriod);

        int nextCount = list.size() + 1;
        WarningRecord warning = new WarningRecord(uuid, nextCount, checkType, now, evidenceId, reason);
        list.add(warning);

        saveWarningsToDisk(uuid, list);
        return warning;
    }

    public synchronized void resetWarnings(UUID uuid) {
        playerWarnings.put(uuid, new ArrayList<>());
        saveWarningsToDisk(uuid, Collections.emptyList());
    }

    private List<WarningRecord> loadWarningsFromDisk(UUID uuid) {
        List<WarningRecord> list = new ArrayList<>();
        File file = getWarningFile(uuid);
        if (!file.exists()) {
            return list;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            try {
                Map<String, Object> map = config.getConfigurationSection(key).getValues(false);
                list.add(WarningRecord.fromMap(map));
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to parse warning for " + uuid + ": " + e.getMessage());
            }
        }
        return list;
    }

    private void saveWarningsToDisk(UUID uuid, List<WarningRecord> list) {
        File file = getWarningFile(uuid);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        YamlConfiguration config = new YamlConfiguration();
        int idx = 1;
        for (WarningRecord record : list) {
            config.createSection("warning-" + idx, record.toMap());
            idx++;
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save warnings for " + uuid + ": " + e.getMessage());
        }
    }

    private File getWarningFile(UUID uuid) {
        File playerDir = new File(new File(plugin.getDataFolder(), "data/players"), uuid.toString());
        return new File(playerDir, "warnings.yml");
    }

    public void cleanup(UUID uuid) {
        playerWarnings.remove(uuid);
    }
}
