package dev.livingyuji.heaveneyes.check.combat;

import dev.livingyuji.heaveneyes.HeavenEyesPlugin;
import dev.livingyuji.heaveneyes.check.AbstractCheck;
import dev.livingyuji.heaveneyes.model.CheckAction;
import dev.livingyuji.heaveneyes.model.CheckType;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * KillAura / attack angle and multi-target anomaly detection module.
 */
public class KillAuraCheck extends AbstractCheck {

    private final Map<UUID, LastHitData> lastHits = new ConcurrentHashMap<>();
    private double maxHitAngle = 90.0;
    private long multiTargetWindowMs = 50L;
    private int thresholdVl = 5;

    public KillAuraCheck(HeavenEyesPlugin plugin) {
        super(plugin, CheckType.KILL_AURA, true, 0.95, CheckAction.WARN_THEN_TEMPBAN);
    }

    public void configure(double maxHitAngle, long multiTargetWindowMs, int thresholdVl) {
        this.maxHitAngle = maxHitAngle;
        this.multiTargetWindowMs = multiTargetWindowMs;
        this.thresholdVl = thresholdVl;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!enabled) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (attacker.getGameMode() == GameMode.CREATIVE || attacker.getGameMode() == GameMode.SPECTATOR) return;
        if (plugin.getPunishmentEngine().isExempt(attacker)) return;

        Entity victim = event.getEntity();
        UUID uuid = attacker.getUniqueId();
        long now = System.currentTimeMillis();

        Location eye = attacker.getEyeLocation();
        Vector look = eye.getDirection().normalize();
        Vector toVictim = victim.getLocation().clone().add(0, victim.getHeight() / 2.0, 0).toVector().subtract(eye.toVector()).normalize();

        double angle = Math.toDegrees(look.angle(toVictim));

        // Check 1: Impossible attack angle (hitting behind player or outside field of vision)
        if (angle > maxHitAngle) {
            double confidence = Math.min(1.0, 0.85 + ((angle - maxHitAngle) / 90.0) * 0.15);
            Map<String, Object> details = new HashMap<>();
            details.put("angle", Math.round(angle * 10.0) / 10.0);
            details.put("max_angle", maxHitAngle);
            details.put("victim_id", victim.getUniqueId().toString());

            String explanation = String.format("Attack angle %.1f exceeds line-of-sight threshold %.1f", angle, maxHitAngle);
            flag(attacker, confidence, explanation, details);
            return;
        }

        // Check 2: Multi-target instantaneous switching
        LastHitData lastHit = lastHits.get(uuid);
        if (lastHit != null) {
            long diff = now - lastHit.timestamp;
            if (diff < multiTargetWindowMs && !lastHit.targetUuid.equals(victim.getUniqueId())) {
                double confidence = 0.96;
                Map<String, Object> details = new HashMap<>();
                details.put("switch_time_ms", diff);
                details.put("prev_target", lastHit.targetUuid.toString());
                details.put("curr_target", victim.getUniqueId().toString());

                String explanation = String.format("Instantaneous target switch within %dms", diff);
                flag(attacker, confidence, explanation, details);
            }
        }

        lastHits.put(uuid, new LastHitData(victim.getUniqueId(), now));
    }

    @Override
    public void cleanup(UUID uuid) {
        super.cleanup(uuid);
        lastHits.remove(uuid);
    }

    private static class LastHitData {
        final UUID targetUuid;
        final long timestamp;

        LastHitData(UUID targetUuid, long timestamp) {
            this.targetUuid = targetUuid;
            this.timestamp = timestamp;
        }
    }
}
