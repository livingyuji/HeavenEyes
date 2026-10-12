package dev.livingyuji.heaveneyes.check.movement;

import dev.livingyuji.heaveneyes.HeavenEyesPlugin;
import dev.livingyuji.heaveneyes.check.AbstractCheck;
import dev.livingyuji.heaveneyes.model.CheckAction;
import dev.livingyuji.heaveneyes.model.CheckType;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side movement speed detection module.
 */
public class SpeedCheck extends AbstractCheck {

    private final Map<UUID, Long> teleportGrace = new ConcurrentHashMap<>();
    private double maxHorizontalSpeed = 0.36;
    private double sprintJumpMultiplier = 1.45;
    private double speedEffectMultiplier = 0.20;
    private double iceMultiplier = 1.60;
    private int thresholdVl = 5;

    public SpeedCheck(HeavenEyesPlugin plugin) {
        super(plugin, CheckType.SPEED, true, 0.90, CheckAction.WARN_THEN_TEMPBAN);
    }

    public void configure(double maxSpeed, double sprintMult, double speedMult, double iceMult, int thresholdVl) {
        this.maxHorizontalSpeed = maxSpeed;
        this.sprintJumpMultiplier = sprintMult;
        this.speedEffectMultiplier = speedMult;
        this.iceMultiplier = iceMult;
        this.thresholdVl = thresholdVl;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        teleportGrace.put(event.getPlayer().getUniqueId(), System.currentTimeMillis() + 1500L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!enabled) return;
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isGliding() || player.isInsideVehicle() || player.isRiptiding()) return;
        if (plugin.getPunishmentEngine().isExempt(player)) return;

        UUID uuid = player.getUniqueId();
        Long grace = teleportGrace.get(uuid);
        if (grace != null && System.currentTimeMillis() < grace) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null || from.getWorld() != to.getWorld()) return;

        double deltaX = to.getX() - from.getX();
        double deltaZ = to.getZ() - from.getZ();
        double horizontalDistance = Math.hypot(deltaX, deltaZ);

        double maxAllowed = maxHorizontalSpeed;
        if (player.isSprinting()) {
            maxAllowed *= sprintJumpMultiplier;
        }

        if (player.hasPotionEffect(PotionEffectType.SPEED)) {
            var effect = player.getPotionEffect(PotionEffectType.SPEED);
            int amp = effect != null ? effect.getAmplifier() + 1 : 1;
            maxAllowed += (amp * speedEffectMultiplier);
        }

        Block blockBelow = player.getLocation().clone().subtract(0, 0.5, 0).getBlock();
        if (blockBelow.getType() == Material.ICE || blockBelow.getType() == Material.PACKED_ICE || blockBelow.getType() == Material.BLUE_ICE) {
            maxAllowed *= iceMultiplier;
        }

        if (horizontalDistance > maxAllowed) {
            double excess = horizontalDistance - maxAllowed;
            double confidence = Math.min(1.0, 0.70 + (excess * 1.5));

            Map<String, Object> details = new HashMap<>();
            details.put("distance", Math.round(horizontalDistance * 1000.0) / 1000.0);
            details.put("max_allowed", Math.round(maxAllowed * 1000.0) / 1000.0);
            details.put("excess", Math.round(excess * 1000.0) / 1000.0);
            details.put("sprinting", player.isSprinting());

            String explanation = String.format("Horizontal speed %.3f exceeded max allowed %.3f", horizontalDistance, maxAllowed);
            flag(player, confidence, explanation, details);
        }
    }

    @Override
    public void cleanup(UUID uuid) {
        super.cleanup(uuid);
        teleportGrace.remove(uuid);
    }
}
