package dev.livingyuji.heaveneyes.config;

import dev.livingyuji.heaveneyes.HeavenEyesPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StaffConfig {

    private final HeavenEyesPlugin plugin;
    private final File file;
    private final Map<UUID, String> staffMembers = new ConcurrentHashMap<>();

    public StaffConfig(HeavenEyesPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "staff.yml");
    }

    public void load() {
        staffMembers.clear();
        if (!file.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<Map<?, ?>> list = config.getMapList("staff.members");
        for (Map<?, ?> entry : list) {
            try {
                String uuidStr = (String) entry.get("uuid");
                String name = (String) entry.get("name");
                if (uuidStr != null) {
                    staffMembers.put(UUID.fromString(uuidStr), name != null ? name : "Staff");
                }
            } catch (Exception ignored) {}
        }
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<UUID, String> entry : staffMembers.entrySet()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("uuid", entry.getKey().toString());
            map.put("name", entry.getValue());
            map.put("alerts", plugin.getAlertManager().isAlertsEnabledFor(entry.getKey()));
            list.add(map);
        }
        config.set("staff.members", list);
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save staff.yml: " + e.getMessage());
        }
    }

    public boolean addStaff(UUID uuid, String name) {
        if (staffMembers.containsKey(uuid)) return false;
        staffMembers.put(uuid, name);
        save();
        return true;
    }

    public boolean removeStaff(UUID uuid) {
        if (staffMembers.remove(uuid) != null) {
            save();
            return true;
        }
        return false;
    }

    public boolean isStaff(UUID uuid) {
        return staffMembers.containsKey(uuid);
    }

    public Map<UUID, String> getStaffMembers() {
        return Collections.unmodifiableMap(staffMembers);
    }
}
