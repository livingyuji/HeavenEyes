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
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side anti-knockback / velocity anomaly detection module.
 */
public class VelocityCheck extends AbstractCheck {

    private final Map<UUID, VelocityExpectation> pendingVelocity = new ConcurrentHashMap<>();
    private int knockbackGraceTicks = 25;
    private int thresholdVl = 4;

    public VelocityCheck(HeavenEyesPlugin plugin) {
        super(plugin, CheckType.VELOCITY, true, 0.90, CheckAction.WARN_THEN_TEMPBAN);
    }

    public void configure(int knockbackGraceTicks, int thresholdVl) {
        this.knockbackGraceTicks = knockbackGraceTicks;
        this.thresholdVl = thresholdVl;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVelocity(PlayerVelocityEvent event) {
        if (!enabled) return;
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (plugin.getPunishmentEngine().isExempt(player)) return;

        Vector vec = event.getVelocity();
        // Only track significant vertical or horizontal knockback
        if (vec.length() > 0.25) {
            pendingVelocity.put(player.getUniqueId(), new VelocityExpectation(vec, System.currentTimeMillis()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!enabled) return;
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        VelocityExpectation exp = pendingVelocity.get(uuid);
        if (exp == null) return;

        long elapsed = System.currentTimeMillis() - exp.timestamp;
        if (elapsed > 1200L) {
            // Expired without responding
            pendingVelocity.remove(uuid);
            return;
        }

        Vector actualMovement = event.getTo().toVector().subtract(event.getFrom().toVector());
        // If expected knockback was strong and actual movement is virtually 0
        if (exp.vector.length() > 0.4 && actualMovement.length() < 0.05 && elapsed > 200L) {
            exp.missedTicks++;
            if (exp.missedTicks >= 3) {
                pendingVelocity.remove(uuid);
                double confidence = 0.91;
                Map<String, Object> details = new HashMap<>();
                details.put("expected_velocity", exp.vector.toString());
                details.put("actual_velocity", actualMovement.toString());

                String explanation = String.format("Player ignored server velocity impulse (expected %.2f, moved %.2f)",
                        exp.vector.length(), actualMovement.length());
                flag(player, confidence, explanation, details);
            }
        }
    }

    @Override
    public void cleanup(UUID uuid) {
        super.cleanup(uuid);
        pendingVelocity.remove(uuid);
    }

    private static class VelocityExpectation {
        final Vector vector;
        final long timestamp;
        int missedTicks = 0;

        VelocityExpectation(Vector vector, long timestamp) {
            this.vector = vector;
            this.timestamp = timestamp;
        }
    }
}
