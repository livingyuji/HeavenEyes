package dev.livingyuji.heaveneyes.check.combat;

import dev.livingyuji.heaveneyes.HeavenEyesPlugin;
import dev.livingyuji.heaveneyes.check.AbstractCheck;
import dev.livingyuji.heaveneyes.model.CheckAction;
import dev.livingyuji.heaveneyes.model.CheckType;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Autoclicker / high CPS anomaly detection module.
 */
public class FastClickCheck extends AbstractCheck {

    private final Map<UUID, ClickData> clickMap = new ConcurrentHashMap<>();
    private int maxCps = 20;
    private int burstThreshold = 24;
    private int thresholdVl = 5;

    public FastClickCheck(HeavenEyesPlugin plugin) {
        super(plugin, CheckType.FAST_CLICK, true, 0.95, CheckAction.WARN_THEN_TEMPBAN);
    }

    public void configure(int maxCps, int burstThreshold, int thresholdVl) {
        this.maxCps = maxCps;
        this.burstThreshold = burstThreshold;
        this.thresholdVl = thresholdVl;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onArmAnimation(PlayerAnimationEvent event) {
        if (!enabled) return;
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) return;
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (plugin.getPunishmentEngine().isExempt(player)) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        ClickData data = clickMap.computeIfAbsent(uuid, k -> new ClickData(now));
        if (now - data.windowStart >= 1000L) {
            int cps = data.clicks;
            data.windowStart = now;
            data.clicks = 1;

            if (cps >= maxCps) {
                double confidence = Math.min(1.0, 0.85 + ((cps - maxCps) * 0.04));
                Map<String, Object> details = new HashMap<>();
                details.put("cps", cps);
                details.put("max_cps", maxCps);

                String explanation = String.format("Autoclicker detected: %d CPS exceeds limit %d", cps, maxCps);
                flag(player, confidence, explanation, details);
            }
        } else {
            data.clicks++;
            if (data.clicks >= burstThreshold) {
                double confidence = 0.98;
                Map<String, Object> details = new HashMap<>();
                details.put("burst_clicks", data.clicks);
                details.put("threshold", burstThreshold);

                String explanation = String.format("Extreme autoclicker burst: %d clicks in sub-second window", data.clicks);
                flag(player, confidence, explanation, details);
            }
        }
    }

    @Override
    public void cleanup(UUID uuid) {
        super.cleanup(uuid);
        clickMap.remove(uuid);
    }

    private static class ClickData {
        long windowStart;
        int clicks;

        ClickData(long windowStart) {
            this.windowStart = windowStart;
            this.clicks = 1;
        }
    }
}
