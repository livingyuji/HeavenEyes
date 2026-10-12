package dev.livingyuji.heaveneyes.check.combat;

import dev.livingyuji.heaveneyes.HeavenEyesPlugin;
import dev.livingyuji.heaveneyes.check.AbstractCheck;
import dev.livingyuji.heaveneyes.model.CheckAction;
import dev.livingyuji.heaveneyes.model.CheckType;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Combat critical hits anomaly detection module.
 */
public class CriticalsCheck extends AbstractCheck {

    private boolean requireAirFall = true;
    private int thresholdVl = 3;

    public CriticalsCheck(HeavenEyesPlugin plugin) {
        super(plugin, CheckType.CRITICALS, true, 0.95, CheckAction.WARN_THEN_TEMPBAN);
    }

    public void configure(boolean requireAirFall, int thresholdVl) {
        this.requireAirFall = requireAirFall;
        this.thresholdVl = thresholdVl;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!enabled) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (attacker.getGameMode() == GameMode.CREATIVE || attacker.getGameMode() == GameMode.SPECTATOR) return;
        if (plugin.getPunishmentEngine().isExempt(attacker)) return;

        // In 1.21 Paper, attacker.isCritical() indicates critical strike
        // If critical hit occurs when attacker is firmly on ground or in water or on ladder
        if (attacker.getFallDistance() > 0.0F && attacker.isOnGround()) {
            Material mat = attacker.getLocation().getBlock().getType();
            if (mat != Material.LADDER && mat != Material.VINE && mat != Material.WATER && mat != Material.COBWEB) {
                double confidence = 0.94;
                Map<String, Object> details = new HashMap<>();
                details.put("fall_distance", attacker.getFallDistance());
                details.put("is_on_ground", true);

                String explanation = String.format("Spoofed critical strike while standing on solid ground (fallDistance %.2f)",
                        attacker.getFallDistance());
                flag(attacker, confidence, explanation, details);
            }
        }
    }
}
