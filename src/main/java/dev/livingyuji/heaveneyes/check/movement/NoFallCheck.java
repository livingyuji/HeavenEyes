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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side NoFall packet spoofing detection module.
 */
public class NoFallCheck extends AbstractCheck {

    private final Map<UUID, Float> highestFallDistance = new ConcurrentHashMap<>();
    private double minFallDistance = 3.5;
    private int thresholdVl = 3;

    public NoFallCheck(HeavenEyesPlugin plugin) {
        super(plugin, CheckType.NO_FALL, true, 0.90, CheckAction.WARN_THEN_TEMPBAN);
    }

    public void configure(double minFallDistance, int thresholdVl) {
        this.minFallDistance = minFallDistance;
        this.thresholdVl = thresholdVl;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!enabled) return;
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isGliding() || player.isInsideVehicle() || player.isFlying()) return;
        if (plugin.getPunishmentEngine().isExempt(player)) return;

        Location to = event.getTo();
        Location from = event.getFrom();
        if (to == null || from.getWorld() != to.getWorld()) return;

        double deltaY = to.getY() - from.getY();
        UUID uuid = player.getUniqueId();

        // If client claims onGround is true
        if (player.isOnGround()) {
            // Check if there is really any solid block underneath
            boolean actuallyOnGround = hasGroundBeneath(to);

            if (!actuallyOnGround && deltaY < -0.3) {
                float fallDist = player.getFallDistance();
                if (fallDist > minFallDistance) {
                    double confidence = 0.95;
                    Map<String, Object> details = new HashMap<>();
                    details.put("claimed_on_ground", true);
                    details.put("fall_distance", fallDist);
                    details.put("delta_y", Math.round(deltaY * 1000.0) / 1000.0);

                    String explanation = String.format("Spoofed on-ground status in mid-air (fall distance: %.2f)", fallDist);
                    flag(player, confidence, explanation, details);
                }
            }
            highestFallDistance.remove(uuid);
        } else {
            highestFallDistance.put(uuid, player.getFallDistance());
        }
    }

    private boolean hasGroundBeneath(Location loc) {
        for (double x = -0.3; x <= 0.3; x += 0.3) {
            for (double z = -0.3; z <= 0.3; z += 0.3) {
                Block b = loc.clone().add(x, -0.2, z).getBlock();
                if (b.getType().isSolid() || b.isLiquid() || b.getType() == Material.COBWEB
                        || b.getType() == Material.LADDER || b.getType() == Material.SCAFFOLDING) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void cleanup(UUID uuid) {
        super.cleanup(uuid);
        highestFallDistance.remove(uuid);
    }
}
