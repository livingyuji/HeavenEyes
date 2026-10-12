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
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side flight and unsupported vertical movement detection.
 */
public class FlyCheck extends AbstractCheck {

    private final Map<UUID, Integer> airTicks = new ConcurrentHashMap<>();
    private final Map<UUID, Double> lastYDelta = new ConcurrentHashMap<>();
    private int maxAirTicks = 30;
    private double maxAscentRate = 0.42;

    public FlyCheck(HeavenEyesPlugin plugin) {
        super(plugin, CheckType.FLIGHT, true, 0.90, CheckAction.WARN_THEN_TEMPBAN);
    }

    public void configure(int maxAirTicks, double maxAscentRate) {
        this.maxAirTicks = maxAirTicks;
        this.maxAscentRate = maxAscentRate;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!enabled) return;
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isGliding() || player.isInsideVehicle() || player.isRiptiding() || player.isFlying()) return;
        if (player.hasPotionEffect(PotionEffectType.LEVITATION) || player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) return;
        if (plugin.getPunishmentEngine().isExempt(player)) return;

        Location to = event.getTo();
        Location from = event.getFrom();
        if (to == null || from.getWorld() != to.getWorld()) return;

        UUID uuid = player.getUniqueId();
        double deltaY = to.getY() - from.getY();

        boolean onGround = player.isOnGround() || isBlockNearby(player.getLocation());

        if (onGround) {
            airTicks.remove(uuid);
            lastYDelta.remove(uuid);
            return;
        }

        int currentAirTicks = airTicks.compute(uuid, (k, ticks) -> ticks == null ? 1 : ticks + 1);

        // Check 1: Excessive ascent without ladder or jump
        if (deltaY > maxAscentRate && !isClimbable(player.getLocation())) {
            double confidence = 0.92;
            Map<String, Object> details = new HashMap<>();
            details.put("delta_y", Math.round(deltaY * 1000.0) / 1000.0);
            details.put("max_ascent", maxAscentRate);
            details.put("air_ticks", currentAirTicks);

            String explanation = String.format("Unsupported ascent rate %.3f (max %.3f)", deltaY, maxAscentRate);
            flag(player, confidence, explanation, details);
            return;
        }

        // Check 2: Prolonged hovering / floating in air
        if (currentAirTicks > maxAirTicks) {
            Double prevDeltaY = lastYDelta.get(uuid);
            if (prevDeltaY != null && Math.abs(deltaY) < 0.05 && Math.abs(prevDeltaY) < 0.05) {
                double confidence = 0.94;
                Map<String, Object> details = new HashMap<>();
                details.put("air_ticks", currentAirTicks);
                details.put("delta_y", Math.round(deltaY * 1000.0) / 1000.0);

                String explanation = String.format("Hovering in air without support for %d ticks", currentAirTicks);
                flag(player, confidence, explanation, details);
            }
        }

        lastYDelta.put(uuid, deltaY);
    }

    private boolean isBlockNearby(Location loc) {
        Block below = loc.clone().subtract(0, 0.6, 0).getBlock();
        return below.getType().isSolid() || below.isLiquid();
    }

    private boolean isClimbable(Location loc) {
        Material mat = loc.getBlock().getType();
        return mat == Material.LADDER || mat == Material.VINE || mat == Material.SCAFFOLDING
                || mat == Material.WEEPING_VINES || mat == Material.TWISTING_VINES;
    }

    @Override
    public void cleanup(UUID uuid) {
        super.cleanup(uuid);
        airTicks.remove(uuid);
        lastYDelta.remove(uuid);
    }
}
