package dev.livingyuji.heaveneyes.check.esp;

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
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Server-side heuristic for suspicious container/chest interactions through occlusion or extreme distance.
 * Defaults to REVIEW mode.
 */
public class ContainerEspCheck extends AbstractCheck {

    private boolean wallOcclusionCheck = true;
    private double maxContainerReach = 5.20;
    private int thresholdVl = 3;

    public ContainerEspCheck(HeavenEyesPlugin plugin) {
        super(plugin, CheckType.CONTAINER_ESP, true, 0.85, CheckAction.REVIEW);
    }

    public void configure(boolean wallOcclusionCheck, double maxContainerReach, int thresholdVl) {
        this.wallOcclusionCheck = wallOcclusionCheck;
        this.maxContainerReach = maxContainerReach;
        this.thresholdVl = thresholdVl;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!enabled) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        Material type = clicked.getType();
        if (!isContainer(type)) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (plugin.getPunishmentEngine().isExempt(player)) return;

        Location eye = player.getEyeLocation();
        Location target = clicked.getLocation().clone().add(0.5, 0.5, 0.5);
        double distance = eye.distance(target);

        // Reach check on container
        if (distance > maxContainerReach) {
            double confidence = 0.88;
            Map<String, Object> details = new HashMap<>();
            details.put("distance", Math.round(distance * 1000.0) / 1000.0);
            details.put("max_reach", maxContainerReach);
            details.put("container", type.name());

            String explanation = String.format("Container interaction at distance %.2f exceeding limit %.2f", distance, maxContainerReach);
            flag(player, confidence, explanation, details);
            return;
        }

        // Occlusion heuristic: check if direct line of sight has multiple solid blocks in between
        if (wallOcclusionCheck && distance > 2.0) {
            int solidBlocksBetween = countSolidBlocksBetween(eye, target);
            if (solidBlocksBetween >= 2) {
                double confidence = 0.82;
                Map<String, Object> details = new HashMap<>();
                details.put("occluding_blocks", solidBlocksBetween);
                details.put("container", type.name());
                details.put("distance", Math.round(distance * 10.0) / 10.0);

                String explanation = String.format("Container interacted through %d solid occluding blocks (heuristic review)", solidBlocksBetween);
                flag(player, confidence, explanation, details);
            }
        }
    }

    private boolean isContainer(Material mat) {
        return mat == Material.CHEST || mat == Material.TRAPPED_CHEST || mat == Material.BARREL
                || mat == Material.SHULKER_BOX || mat == Material.ENDER_CHEST
                || mat.name().endsWith("_SHULKER_BOX");
    }

    private int countSolidBlocksBetween(Location start, Location end) {
        int solidCount = 0;
        int steps = 10;
        double dx = (end.getX() - start.getX()) / steps;
        double dy = (end.getY() - start.getY()) / steps;
        double dz = (end.getZ() - start.getZ()) / steps;

        Location curr = start.clone();
        for (int i = 1; i < steps - 1; i++) {
            curr.add(dx, dy, dz);
            Block b = curr.getBlock();
            if (b.getType().isOccluding() && !isContainer(b.getType())) {
                solidCount++;
            }
        }
        return solidCount;
    }
}
