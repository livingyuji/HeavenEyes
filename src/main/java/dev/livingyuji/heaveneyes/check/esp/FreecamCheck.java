package dev.livingyuji.heaveneyes.check.esp;

import dev.livingyuji.heaveneyes.HeavenEyesPlugin;
import dev.livingyuji.heaveneyes.check.AbstractCheck;
import dev.livingyuji.heaveneyes.model.CheckAction;
import dev.livingyuji.heaveneyes.model.CheckType;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Server-side heuristic module for suspicious Freecam interaction patterns.
 * Defaults to REVIEW mode.
 */
public class FreecamCheck extends AbstractCheck {

    private double maxInteractionDistance = 6.0;
    private double bodyMovementDistance = 0.1;
    private int thresholdVl = 3;

    public FreecamCheck(HeavenEyesPlugin plugin) {
        super(plugin, CheckType.FREECAM, true, 0.85, CheckAction.REVIEW);
    }

    public void configure(double maxInteractionDistance, double bodyMovementDistance, int thresholdVl) {
        this.maxInteractionDistance = maxInteractionDistance;
        this.bodyMovementDistance = bodyMovementDistance;
        this.thresholdVl = thresholdVl;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!enabled) return;
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (plugin.getPunishmentEngine().isExempt(player)) return;

        Block block = event.getBlock();
        Location eye = player.getEyeLocation();
        Location target = block.getLocation().clone().add(0.5, 0.5, 0.5);

        double distance = eye.distance(target);
        if (distance > maxInteractionDistance) {
            double confidence = 0.85;
            Map<String, Object> details = new HashMap<>();
            details.put("distance", Math.round(distance * 100.0) / 100.0);
            details.put("max_allowed", maxInteractionDistance);
            details.put("block", block.getType().name());

            String explanation = String.format("Block broken at distance %.2f exceeding standard range %.2f",
                    distance, maxInteractionDistance);
            flag(player, confidence, explanation, details);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!enabled) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (plugin.getPunishmentEngine().isExempt(player)) return;

        Location eye = player.getEyeLocation();
        Location target = block.getLocation().clone().add(0.5, 0.5, 0.5);

        double distance = eye.distance(target);
        if (distance > maxInteractionDistance + 0.5) {
            double confidence = 0.82;
            Map<String, Object> details = new HashMap<>();
            details.put("distance", Math.round(distance * 100.0) / 100.0);
            details.put("max_allowed", maxInteractionDistance);
            details.put("block", block.getType().name());

            String explanation = String.format("Interaction at remote distance %.2f without physical approach", distance);
            flag(player, confidence, explanation, details);
        }
    }
}
