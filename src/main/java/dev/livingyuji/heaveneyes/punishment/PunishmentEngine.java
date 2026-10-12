package dev.livingyuji.heaveneyes.punishment;

import dev.livingyuji.heaveneyes.HeavenEyesPlugin;
import dev.livingyuji.heaveneyes.check.AbstractCheck;
import dev.livingyuji.heaveneyes.model.*;
import dev.livingyuji.heaveneyes.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core punishment engine evaluating detection confidence, warning progression,
 * repeated detection windows, and automated enforcement actions.
 */
public class PunishmentEngine {

    private final HeavenEyesPlugin plugin;
    private final Set<UUID> exemptPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Map<CheckType, Long>> lastActionTimestamps = new ConcurrentHashMap<>();
    private final Set<UUID> pendingPunishments = ConcurrentHashMap.newKeySet();

    public PunishmentEngine(HeavenEyesPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isExempt(Player player) {
        if (player.hasPermission("heaveneyes.exempt")) return true;
        if (exemptPlayers.contains(player.getUniqueId())) return true;
        if (plugin.getPluginConfig().isExemptCreative() && player.getGameMode() == GameMode.CREATIVE) return true;
        if (plugin.getPluginConfig().isExemptSpectator() && player.getGameMode() == GameMode.SPECTATOR) return true;
        return false;
    }

    public void setExempt(UUID uuid, boolean exempt) {
        if (exempt) {
            exemptPlayers.add(uuid);
        } else {
            exemptPlayers.remove(uuid);
        }
    }

    public boolean isExempt(UUID uuid) {
        return exemptPlayers.contains(uuid);
    }

    public void handleViolation(Player player, AbstractCheck check, Violation violation) {
        UUID uuid = player.getUniqueId();
        if (isExempt(player)) return;

        CheckAction action = check.getAction();
        if (action == CheckAction.NONE) return;

        // Verify minimum confidence threshold
        if (plugin.getPunishmentsConfig().isRequireConfidenceThreshold()
                && violation.getConfidence() < check.getMinimumConfidence()) {
            if (plugin.isDebugEnabled()) {
                plugin.getLogger().info(String.format("[DEBUG] Violation for %s on %s dropped: confidence %.2f < %.2f",
                        player.getName(), check.getName(), violation.getConfidence(), check.getMinimumConfidence()));
            }
            return;
        }

        // Send real-time staff alert
        plugin.getAlertManager().sendAlert(player, check, violation);

        // Save incident evidence
        String incidentId = plugin.getEvidenceManager().saveIncident(violation, action.name(), null);

        // If check is REVIEW only, we record and alert, but do not automatically punish
        if (action == CheckAction.REVIEW) {
            return;
        }

        // Automatic punishment global toggle
        if (!plugin.getPunishmentsConfig().isAutomaticEnabled()) {
            return;
        }

        // Rate limiting repeated actions within detection window
        long now = System.currentTimeMillis();
        long window = plugin.getPunishmentsConfig().getRepeatedDetectionWindowMillis();
        Map<CheckType, Long> playerTimestamps = lastActionTimestamps.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
        Long lastAction = playerTimestamps.get(check.getType());

        if (lastAction != null && (now - lastAction) < window) {
            // Rate-limited; log and return
            return;
        }

        // Prevent duplicate punishment race conditions
        if (plugin.getPunishmentsConfig().isPreventDuplicatePunishments() && !pendingPunishments.add(uuid)) {
            return;
        }

        try {
            executePunishmentPolicy(player, check, violation, incidentId, playerTimestamps, now);
        } finally {
            pendingPunishments.remove(uuid);
        }
    }

    private void executePunishmentPolicy(Player player, AbstractCheck check, Violation violation,
                                         String incidentId, Map<CheckType, Long> playerTimestamps, long now) {
        UUID uuid = player.getUniqueId();
        WarningManager wm = plugin.getWarningManager();
        int maxWarnings = plugin.getPunishmentsConfig().getWarningsBeforeTempban();
        int activeWarningCount = wm.getActiveWarningCount(uuid);

        // Severe detection immediate kick check
        if (plugin.getPunishmentsConfig().isSevereKickEnabled() && violation.getConfidence() >= 0.98) {
            playerTimestamps.put(check.getType(), now);
            executeKick(player, "Severe suspicious activity: " + check.getName(), incidentId);
            return;
        }

        // Graduated warning progression
        if (activeWarningCount < maxWarnings) {
            WarningRecord warning = wm.addWarning(uuid, check.getType(), incidentId, check.getName() + " violation");
            playerTimestamps.put(check.getType(), now);

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("count", String.valueOf(warning.getCount()));
            placeholders.put("maximum", String.valueOf(maxWarnings));
            placeholders.put("player", player.getName());
            placeholders.put("check", check.getName());

            String msg = TextUtil.format(plugin.getPunishmentsConfig().getWarningMessage(), placeholders);
            player.sendMessage(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(msg));

            plugin.getAlertManager().sendActionAlert(player, "WARNING " + warning.getCount() + "/" + maxWarnings, check.getName());
        } else {
            // Warning limit reached -> automatic tempban
            playerTimestamps.put(check.getType(), now);
            long tempbanDuration = plugin.getPunishmentsConfig().getTempbanDurationMillis();
            String reason = String.format("Automated detection: repeated %s violations", check.getName());

            BanRecord ban = plugin.getBanManager().createBan(
                    uuid,
                    player.getName(),
                    BanRecord.Type.TEMPORARY,
                    reason,
                    BanRecord.Source.AUTOMATIC,
                    check.getType(),
                    tempbanDuration,
                    "HeavenEyes",
                    incidentId
            );

            // Reset warnings on ban
            wm.resetWarnings(uuid);

            plugin.getAlertManager().sendActionAlert(player, "TEMPBAN (" + plugin.getPunishmentsConfig().getTempbanDurationString() + ")", "Ban ID: " + ban.getBanId());
        }
    }

    public void executeKick(Player player, String reason, String incidentId) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        placeholders.put("reason", reason);
        placeholders.put("incident_id", incidentId != null ? incidentId : "N/A");

        String msg = TextUtil.format(plugin.getPunishmentsConfig().getKickMessage(), placeholders);
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.kick(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(msg));
        });

        plugin.getAuditLogger().logAction(String.format("KICK_EXECUTED player=%s(%s) reason='%s' incident=%s",
                player.getName(), player.getUniqueId(), reason, incidentId));
    }

    public void cleanup(UUID uuid) {
        lastActionTimestamps.remove(uuid);
        pendingPunishments.remove(uuid);
    }
}
