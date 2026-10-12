package dev.livingyuji.heaveneyes.check.combat;

import dev.livingyuji.heaveneyes.HeavenEyesPlugin;
import dev.livingyuji.heaveneyes.check.AbstractCheck;
import dev.livingyuji.heaveneyes.model.CheckAction;
import dev.livingyuji.heaveneyes.model.CheckType;
import dev.livingyuji.heaveneyes.util.MathUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AimSnap / instantaneous rotation snap anomaly detection module.
 */
public class AimSnapCheck extends AbstractCheck {

    private final Map<UUID, Float> lastYaw = new ConcurrentHashMap<>();
    private final Map<UUID, Float> lastPitch = new ConcurrentHashMap<>();
    private final Map<UUID, Float> lastYawDelta = new ConcurrentHashMap<>();
    private double maxAngleSnap = 85.0;
    private int thresholdVl = 4;

    public AimSnapCheck(HeavenEyesPlugin plugin) {
        super(plugin, CheckType.AIM_SNAP, true, 0.95, CheckAction.WARN_THEN_TEMPBAN);
    }

    public void configure(double maxAngleSnap, int thresholdVl) {
        this.maxAngleSnap = maxAngleSnap;
        this.thresholdVl = thresholdVl;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!enabled) return;
        UUID uuid = event.getPlayer().getUniqueId();
        float currentYaw = event.getTo().getYaw();
        float prevYaw = lastYaw.getOrDefault(uuid, currentYaw);

        float delta = MathUtil.getAngleDistance(prevYaw, currentYaw);
        lastYawDelta.put(uuid, delta);
        lastYaw.put(uuid, currentYaw);
        lastPitch.put(uuid, event.getTo().getPitch());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!enabled) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (attacker.getGameMode() == GameMode.CREATIVE || attacker.getGameMode() == GameMode.SPECTATOR) return;
        if (plugin.getPunishmentEngine().isExempt(attacker)) return;

        UUID uuid = attacker.getUniqueId();
        Float yawDelta = lastYawDelta.get(uuid);

        if (yawDelta != null && yawDelta > maxAngleSnap) {
            double confidence = 0.92;
            Map<String, Object> details = new HashMap<>();
            details.put("yaw_delta", Math.round(yawDelta * 10.0) / 10.0);
            details.put("max_snap", maxAngleSnap);

            String explanation = String.format("Instant aim snap rotation %.1f degrees on attack", yawDelta);
            flag(attacker, confidence, explanation, details);
        }
    }

    @Override
    public void cleanup(UUID uuid) {
        super.cleanup(uuid);
        lastYaw.remove(uuid);
        lastPitch.remove(uuid);
        lastYawDelta.remove(uuid);
    }
}
