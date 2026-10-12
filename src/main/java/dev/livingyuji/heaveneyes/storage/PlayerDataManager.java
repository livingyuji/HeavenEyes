package dev.livingyuji.heaveneyes.storage;

import dev.livingyuji.heaveneyes.HeavenEyesPlugin;
import dev.livingyuji.heaveneyes.model.MiningProfile;
import dev.livingyuji.heaveneyes.model.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player-specific folders, profiles, mining records, and history persistence.
 */
public class PlayerDataManager {

    private final HeavenEyesPlugin plugin;
    private final File playersFolder;

    private final Map<UUID, PlayerProfile> profileCache = new ConcurrentHashMap<>();
    private final Map<UUID, MiningProfile> miningCache = new ConcurrentHashMap<>();
    private final Map<String, UUID> nameToUuidMap = new ConcurrentHashMap<>();

    public PlayerDataManager(HeavenEyesPlugin plugin) {
        this.plugin = plugin;
        this.playersFolder = new File(plugin.getDataFolder(), "data/players");
    }

    public void initialize() {
        if (!playersFolder.exists()) {
            playersFolder.mkdirs();
        }
    }

    public File getPlayerDirectory(UUID uuid) {
        File dir = new File(playersFolder, uuid.toString());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public PlayerProfile getPlayerProfile(UUID uuid, String name) {
        if (uuid == null) return null;
        if (name != null) {
            nameToUuidMap.put(name.toLowerCase(Locale.ROOT), uuid);
        }

        return profileCache.computeIfAbsent(uuid, id -> {
            File profileFile = new File(getPlayerDirectory(id), "profile.yml");
            if (profileFile.exists()) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(profileFile);
                return PlayerProfile.fromMap(config.getValues(false));
            } else {
                PlayerProfile created = new PlayerProfile(id, name != null ? name : "Unknown");
                saveProfileAsync(created);
                return created;
            }
        });
    }

    public PlayerProfile getPlayerProfile(UUID uuid) {
        return getPlayerProfile(uuid, null);
    }

    public UUID getUuidByName(String name) {
        if (name == null) return null;
        UUID cached = nameToUuidMap.get(name.toLowerCase(Locale.ROOT));
        if (cached != null) return cached;

        // Try offline player lookup from server
        var offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (offlinePlayer != null && offlinePlayer.getUniqueId() != null) {
            nameToUuidMap.put(name.toLowerCase(Locale.ROOT), offlinePlayer.getUniqueId());
            return offlinePlayer.getUniqueId();
        }
        return null;
    }

    public MiningProfile getMiningProfile(UUID uuid) {
        if (uuid == null) return null;
        return miningCache.computeIfAbsent(uuid, id -> {
            File miningFile = new File(getPlayerDirectory(id), "mining.yml");
            if (miningFile.exists()) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(miningFile);
                return MiningProfile.fromMap(config.getValues(false));
            } else {
                MiningProfile created = new MiningProfile(id);
                saveMiningAsync(created);
                return created;
            }
        });
    }

    public void saveProfileAsync(PlayerProfile profile) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            File profileFile = new File(getPlayerDirectory(profile.getUuid()), "profile.yml");
            YamlConfiguration config = new YamlConfiguration();
            for (Map.Entry<String, Object> entry : profile.toMap().entrySet()) {
                config.set(entry.getKey(), entry.getValue());
            }
            try {
                config.save(profileFile);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save profile for " + profile.getUuid() + ": " + e.getMessage());
            }
        });
    }

    public void saveMiningAsync(MiningProfile profile) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            File miningFile = new File(getPlayerDirectory(profile.getUuid()), "mining.yml");
            YamlConfiguration config = new YamlConfiguration();
            for (Map.Entry<String, Object> entry : profile.toMap().entrySet()) {
                config.set(entry.getKey(), entry.getValue());
            }
            try {
                config.save(miningFile);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save mining profile for " + profile.getUuid() + ": " + e.getMessage());
            }
        });
    }

    public void recordPlayerEvidence(UUID uuid, String incidentId, String checkName) {
        File evidenceFile = new File(getPlayerDirectory(uuid), "evidence.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(evidenceFile);
        List<String> list = config.getStringList("incidents");
        list.add(incidentId + " (" + checkName + " - " + System.currentTimeMillis() + ")");
        config.set("incidents", list);
        try {
            config.save(evidenceFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to update evidence.yml for " + uuid + ": " + e.getMessage());
        }
    }

    public List<String> getPlayerEvidenceList(UUID uuid) {
        File evidenceFile = new File(getPlayerDirectory(uuid), "evidence.yml");
        if (!evidenceFile.exists()) return Collections.emptyList();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(evidenceFile);
        return config.getStringList("incidents");
    }

    public void saveAllSync() {
        for (PlayerProfile profile : profileCache.values()) {
            File profileFile = new File(getPlayerDirectory(profile.getUuid()), "profile.yml");
            YamlConfiguration config = new YamlConfiguration();
            for (Map.Entry<String, Object> entry : profile.toMap().entrySet()) {
                config.set(entry.getKey(), entry.getValue());
            }
            try {
                config.save(profileFile);
            } catch (IOException ignored) {}
        }

        for (MiningProfile profile : miningCache.values()) {
            File miningFile = new File(getPlayerDirectory(profile.getUuid()), "mining.yml");
            YamlConfiguration config = new YamlConfiguration();
            for (Map.Entry<String, Object> entry : profile.toMap().entrySet()) {
                config.set(entry.getKey(), entry.getValue());
            }
            try {
                config.save(miningFile);
            } catch (IOException ignored) {}
        }
    }

    public void cleanup(UUID uuid) {
        PlayerProfile profile = profileCache.remove(uuid);
        if (profile != null) {
            saveProfileAsync(profile);
        }
        MiningProfile mining = miningCache.remove(uuid);
        if (mining != null) {
            saveMiningAsync(mining);
        }
    }
}
