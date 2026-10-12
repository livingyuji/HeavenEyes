package dev.livingyuji.heaveneyes.check.movement;

import dev.livingyuji.heaveneyes.HeavenEyesPlugin;
import dev.livingyuji.heaveneyes.check.AbstractCheck;
import dev.livingyuji.heaveneyes.model.CheckAction;
import dev.livingyuji.heaveneyes.model.CheckType;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

/**
 * Server-side step height anomaly detection.
 */
public class StepCheck extends AbstractCheck {

    private double maxStepHeight = 0.60;
    private int thresholdVl = 4;

    public StepCheck(HeavenEyesPlugin plugin) {
        super(plugin, CheckType.STEP, true, 0.90, CheckAction.WARN_THEN_TEMPBAN);
    }

    public void configure(double maxStepHeight, int thresholdVl) {
        this.maxStepHeight = maxStepHeight;
        this.thresholdVl = thresholdVl;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!enabled) return;
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isGliding() || player.isInsideVehicle() || player.isFlying()) return;
        if (player.hasPotionEffect(PotionEffectType.JUMP_BOOST) || player.hasPotionEffect(PotionEffectType.LEVITATION)) return;
        if (plugin.getPunishmentEngine().isExempt(player)) return;

        Location to = event.getTo();
        Location from = event.getFrom();
        if (to == null || from.getWorld() != to.getWorld()) return;

        double deltaY = to.getY() - from.getY();

        // Normal step height over a block is 0.5 or 0.6 (slabs). Stepping 1 full block (deltaY = 1.0) instantly is Step hack.
        if (from.getY() % 1.0 == 0.0 && deltaY > maxStepHeight && deltaY <= 1.2) {
            // Check if player jumped (jumping has vertical velocity usually > 0.42 but gradual curve)
            // If deltaY is 1.0 directly in a single tick while on ground
            if (deltaY > 0.75 && player.isOnGround()) {
                double confidence = 0.92;
                Map<String, Object> details = new HashMap<>();
                details.put("step_height", Math.round(deltaY * 1000.0) / 1000.0);
                details.put("max_allowed", maxStepHeight);

                String explanation = String.format("Instant vertical step %.3f exceeds normal step height %.3f", deltaY, maxStepHeight);
                flag(player, confidence, explanation, details);
            }
        }
    }
}
