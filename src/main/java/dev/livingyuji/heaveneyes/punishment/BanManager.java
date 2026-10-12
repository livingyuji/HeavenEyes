package dev.livingyuji.heaveneyes.punishment;

import dev.livingyuji.heaveneyes.HeavenEyesPlugin;
import dev.livingyuji.heaveneyes.model.BanRecord;
import dev.livingyuji.heaveneyes.model.CheckType;
import dev.livingyuji.heaveneyes.util.DurationParser;
import dev.livingyuji.heaveneyes.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages active and historical bans, ID generation, expiry, and login enforcement.
 */
public class BanManager implements Listener {

    private final HeavenEyesPlugin plugin;
    private final File bansFolder;
    private final File activeFile;
    private final File historyFile;

    private final Map<UUID, BanRecord> activeBans = new ConcurrentHashMap<>();
    private final List<BanRecord> banHistory = Collections.synchronizedList(new ArrayList<>());
    private static final String ID_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public BanManager(HeavenEyesPlugin plugin) {
        this.plugin = plugin;
        this.bansFolder = new File(plugin.getDataFolder(), "bans");
        this.activeFile = new File(bansFolder, "active.yml");
        this.historyFile = new File(bansFolder, "history.yml");
    }

    public void initialize() {
        if (!bansFolder.exists()) {
            bansFolder.mkdirs();
        }
        loadBans();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public synchronized void loadBans() {
        activeBans.clear();
        banHistory.clear();

        if (activeFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(activeFile);
            for (String key : config.getKeys(false)) {
                try {
                    Map<String, Object> map = config.getConfigurationSection(key).getValues(false);
                    BanRecord record = BanRecord.fromMap(map);
                    if (record.isExpired()) {
                        record.setActive(false);
                        banHistory.add(record);
                    } else {
                        activeBans.put(record.getPlayerUuid(), record);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load active ban entry: " + key + " - " + e.getMessage());
                }
            }
        }

        if (historyFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(historyFile);
            for (String key : config.getKeys(false)) {
                try {
                    Map<String, Object> map = config.getConfigurationSection(key).getValues(false);
                    BanRecord record = BanRecord.fromMap(map);
                    banHistory.add(record);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load historical ban entry: " + key + " - " + e.getMessage());
                }
            }
        }
    }

    public synchronized void saveBans() {
        try {
            YamlConfiguration activeConfig = new YamlConfiguration();
            for (BanRecord record : activeBans.values()) {
                activeConfig.createSection(record.getBanId(), record.toMap());
            }
            activeConfig.save(activeFile);

            YamlConfiguration historyConfig = new YamlConfiguration();
            synchronized (banHistory) {
                for (BanRecord record : banHistory) {
                    historyConfig.createSection(record.getBanId(), record.toMap());
                }
            }
            historyConfig.save(historyFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to persist bans: " + e.getMessage());
        }
    }

    public String generateBanId() {
        StringBuilder sb = new StringBuilder("HE-");
        for (int i = 0; i < 6; i++) {
            sb.append(ID_CHARS.charAt(RANDOM.nextInt(ID_CHARS.length())));
        }
        return sb.toString();
    }

    public BanRecord createBan(UUID playerUuid, String playerName, BanRecord.Type type, String reason,
                               BanRecord.Source source, CheckType checkType, long durationMillis,
                               String actor, String evidenceId) {
        long now = System.currentTimeMillis();
        long expiresAt = (type == BanRecord.Type.PERMANENT) ? 0 : (now + durationMillis);
        String banId = generateBanId();

        BanRecord record = new BanRecord(banId, playerUuid, playerName, type, reason, source,
                checkType, now, expiresAt, actor, evidenceId);

        activeBans.put(playerUuid, record);

        // Run async disk save
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveBans);

        // Audit log
        plugin.getAuditLogger().logAction(String.format("BAN_ISSUED id=%s player=%s(%s) type=%s reason='%s' actor=%s",
                banId, playerName, playerUuid, type, reason, actor));

        // Update player profile
        var profile = plugin.getPlayerDataManager().getPlayerProfile(playerUuid, playerName);
        if (profile != null) {
            profile.incrementBans();
        }

        // Kick online player if connected
        Player onlinePlayer = Bukkit.getPlayer(playerUuid);
        if (onlinePlayer != null && onlinePlayer.isOnline()) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                String kickMessage = formatBanKickMessage(record);
                onlinePlayer.kick(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(kickMessage));
            });
        }

        return record;
    }

    public boolean unban(UUID playerUuid, String actor, String reason) {
        BanRecord active = activeBans.remove(playerUuid);
        if (active != null) {
            active.pardon(actor, reason);
            banHistory.add(active);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveBans);

            plugin.getAuditLogger().logAction(String.format("BAN_PARDONED id=%s player=%s(%s) actor=%s reason='%s'",
                    active.getBanId(), active.getPlayerName(), playerUuid, actor, reason));
            return true;
        }
        return false;
    }

    public BanRecord unbanById(String banId, String actor, String reason) {
        for (Map.Entry<UUID, BanRecord> entry : activeBans.entrySet()) {
            if (entry.getValue().getBanId().equalsIgnoreCase(banId)) {
                BanRecord record = entry.getValue();
                activeBans.remove(entry.getKey());
                record.pardon(actor, reason);
                banHistory.add(record);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveBans);

                plugin.getAuditLogger().logAction(String.format("BAN_PARDONED id=%s player=%s(%s) actor=%s reason='%s'",
                        record.getBanId(), record.getPlayerName(), record.getPlayerUuid(), actor, reason));
                return record;
            }
        }
        return null;
    }

    public BanRecord getActiveBan(UUID uuid) {
        BanRecord record = activeBans.get(uuid);
        if (record != null && record.isExpired()) {
            activeBans.remove(uuid);
            record.setActive(false);
            banHistory.add(record);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveBans);
            return null;
        }
        return record;
    }

    public BanRecord getBanById(String banId) {
        for (BanRecord r : activeBans.values()) {
            if (r.getBanId().equalsIgnoreCase(banId)) return r;
        }
        synchronized (banHistory) {
            for (BanRecord r : banHistory) {
                if (r.getBanId().equalsIgnoreCase(banId)) return r;
            }
        }
        return null;
    }

    public List<BanRecord> getPlayerBanHistory(UUID uuid) {
        List<BanRecord> list = new ArrayList<>();
        BanRecord active = activeBans.get(uuid);
        if (active != null) list.add(active);
        synchronized (banHistory) {
            for (BanRecord r : banHistory) {
                if (r.getPlayerUuid().equals(uuid)) list.add(r);
            }
        }
        return list;
    }

    public int getActiveBansCount() {
        return activeBans.size();
    }

    public Collection<BanRecord> getAllActiveBans() {
        return Collections.unmodifiableCollection(activeBans.values());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        BanRecord ban = getActiveBan(uuid);
        if (ban != null) {
            String msg = formatBanKickMessage(ban);
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                    net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(msg));
        }
    }

    public String formatBanKickMessage(BanRecord record) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("ban_id", record.getBanId());
        placeholders.put("player", record.getPlayerName());
        placeholders.put("reason", record.getReason());
        placeholders.put("remaining", DurationParser.format(record.getRemainingMillis()));
        placeholders.put("duration", (record.getType() == BanRecord.Type.PERMANENT) ? "Permanent" : DurationParser.format(record.getExpiresAt() - record.getCreatedAt()));

        String template = (record.getType() == BanRecord.Type.PERMANENT)
                ? plugin.getPunishmentsConfig().getPermbanMessage()
                : plugin.getPunishmentsConfig().getTempbanMessage();

        return TextUtil.format(template, placeholders);
    }
}
