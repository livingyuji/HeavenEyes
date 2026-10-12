package dev.livingyuji.heaveneyes.check.movement;

import dev.livingyuji.heaveneyes.HeavenEyesPlugin;
import dev.livingyuji.heaveneyes.check.AbstractCheck;
import dev.livingyuji.heaveneyes.model.CheckAction;
import dev.livingyuji.heaveneyes.model.CheckType;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side Timer packet-rate anomaly detection.
 */
public class TimerCheck extends AbstractCheck {

    private final Map<UUID, WindowData> playerWindows = new ConcurrentHashMap<>();
    private int maxPacketsPerSecond = 24;
    private int thresholdVl = 6;

    public TimerCheck(HeavenEyesPlugin plugin) {
        super(plugin, CheckType.TIMER, true, 0.90, CheckAction.WARN_THEN_TEMPBAN);
    }

    public void configure(int maxPacketsPerSecond, int thresholdVl) {
        this.maxPacketsPerSecond = maxPacketsPerSecond;
        this.thresholdVl = thresholdVl;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!enabled) return;
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (plugin.getPunishmentEngine().isExempt(player)) return;

        // Ignore events where position didn't change (head rotation only)
        if (event.getFrom().getX() == event.getTo().getX()
                && event.getFrom().getY() == event.getTo().getY()
                && event.getFrom().getZ() == event.getTo().getZ()) {
            return;
        }

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        WindowData data = playerWindows.computeIfAbsent(uuid, k -> new WindowData(now));
        if (now - data.windowStart >= 1000L) {
            int packets = data.packetCount;
            data.windowStart = now;
            data.packetCount = 1;

            if (packets > maxPacketsPerSecond) {
                double confidence = Math.min(1.0, 0.75 + ((packets - maxPacketsPerSecond) * 0.05));
                Map<String, Object> details = new HashMap<>();
                details.put("moves_per_second", packets);
                details.put("max_allowed", maxPacketsPerSecond);

                String explanation = String.format("Movement frequency %d moves/sec exceeded limit %d", packets, maxPacketsPerSecond);
                flag(player, confidence, explanation, details);
            }
        } else {
            data.packetCount++;
        }
    }

    @Override
    public void cleanup(UUID uuid) {
        super.cleanup(uuid);
        playerWindows.remove(uuid);
    }

    private static class WindowData {
        long windowStart;
        int packetCount;

        WindowData(long windowStart) {
            this.windowStart = windowStart;
            this.packetCount = 1;
        }
    }
}
