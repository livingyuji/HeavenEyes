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

import java.util.HashMap;
import java.util.Map;

/**
 * Combat reach distance anomaly detection module.
 */
public class ReachCheck extends AbstractCheck {

    private double maxReachDistance = 3.80;
    private double survivalReachMargin = 0.40;
    private int thresholdVl = 4;

    public ReachCheck(HeavenEyesPlugin plugin) {
        super(plugin, CheckType.REACH, true, 0.95, CheckAction.WARN_THEN_TEMPBAN);
    }

    public void configure(double maxReachDistance, double survivalReachMargin, int thresholdVl) {
        this.maxReachDistance = maxReachDistance;
        this.survivalReachMargin = survivalReachMargin;
        this.thresholdVl = thresholdVl;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!enabled) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (attacker.getGameMode() == GameMode.CREATIVE || attacker.getGameMode() == GameMode.SPECTATOR) return;
        if (plugin.getPunishmentEngine().isExempt(attacker)) return;

        Entity victim = event.getEntity();
        Location attackerEye = attacker.getEyeLocation();
        Location victimLoc = victim.getLocation();

        // Calculate distance from eye to closest point of victim bounding box or center
        double distance = attackerEye.distance(victimLoc.clone().add(0, victim.getHeight() / 2.0, 0));

        // Ping compensation allowance
        int ping = attacker.getPing();
        double pingAllowance = Math.min(0.5, (ping / 1000.0) * 1.5);
        double allowedReach = maxReachDistance + pingAllowance;

        if (distance > allowedReach) {
            double excess = distance - allowedReach;
            double confidence = Math.min(1.0, 0.80 + (excess * 0.4));

            Map<String, Object> details = new HashMap<>();
            details.put("distance", Math.round(distance * 1000.0) / 1000.0);
            details.put("max_allowed", Math.round(allowedReach * 1000.0) / 1000.0);
            details.put("ping", ping);
            details.put("victim_type", victim.getType().name());

            String explanation = String.format("Combat reach %.2f exceeds allowed %.2f (ping %dms)", distance, allowedReach, ping);
            flag(attacker, confidence, explanation, details);
        }
    }
}
