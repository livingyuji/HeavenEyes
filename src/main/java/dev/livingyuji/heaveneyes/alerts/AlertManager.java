package dev.livingyuji.heaveneyes.alerts;

import dev.livingyuji.heaveneyes.HeavenEyesPlugin;
import dev.livingyuji.heaveneyes.check.AbstractCheck;
import dev.livingyuji.heaveneyes.model.Violation;
import dev.livingyuji.heaveneyes.util.TextUtil;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages dispatching formatted staff alerts with rate limiting and recipient preferences.
 */
public class AlertManager {

    private final HeavenEyesPlugin plugin;
    private final Set<UUID> disabledStaffAlerts = ConcurrentHashMap.newKeySet();
    private final Map<String, Long> lastAlertTimestamps = new ConcurrentHashMap<>();

    public AlertManager(HeavenEyesPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isAlertsEnabledFor(UUID uuid) {
        return !disabledStaffAlerts.contains(uuid);
    }

    public void setAlertsEnabled(UUID uuid, boolean enabled) {
        if (enabled) {
            disabledStaffAlerts.remove(uuid);
        } else {
            disabledStaffAlerts.add(uuid);
        }
    }

    public void sendAlert(Player player, AbstractCheck check, Violation violation) {
        if (!plugin.getPluginConfig().isAlertsEnabled()) {
            return;
        }

        // Alert cooldown per player & check
        String key = player.getUniqueId() + ":" + check.getType().name();
        long now = System.currentTimeMillis();
        long cooldown = plugin.getPluginConfig().getAlertCooldownMillis();
        Long lastAlert = lastAlertTimestamps.get(key);

        if (lastAlert != null && (now - lastAlert) < cooldown) {
            return;
        }
        lastAlertTimestamps.put(key, now);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        placeholders.put("check", check.getName());
        placeholders.put("vl", String.valueOf(violation.getViolationLevel()));
        placeholders.put("confidence", String.valueOf((int) (violation.getConfidence() * 100)));
        placeholders.put("details", violation.getExplanation());

        String template = plugin.getMessagesConfig().getStaffAlertTemplate();
        String message = TextUtil.format(template, placeholders);
        var component = LegacyComponentSerializer.legacySection().deserialize(message);

        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("heaveneyes.alerts") && isAlertsEnabledFor(staff.getUniqueId())) {
                staff.sendMessage(component);
            }
        }
    }

    public void sendActionAlert(Player player, String action, String info) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("action", action);
        placeholders.put("player", player.getName());
        placeholders.put("info", info);

        String template = plugin.getMessagesConfig().getStaffAlertActionTemplate();
        String message = TextUtil.format(template, placeholders);
        var component = LegacyComponentSerializer.legacySection().deserialize(message);

        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("heaveneyes.alerts") && isAlertsEnabledFor(staff.getUniqueId())) {
                staff.sendMessage(component);
            }
        }
    }

    public void sendReportAlert(String reporter, String target, String reason) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("reporter", reporter);
        placeholders.put("target", target);
        placeholders.put("reason", reason);

        String template = plugin.getMessagesConfig().getReportAlertTemplate();
        String message = TextUtil.format(template, placeholders);
        var component = LegacyComponentSerializer.legacySection().deserialize(message);

        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("heaveneyes.alerts")) {
                staff.sendMessage(component);
            }
        }
    }
}
