package dev.livingyuji.heaveneyes.check.mining;

import dev.livingyuji.heaveneyes.HeavenEyesPlugin;
import dev.livingyuji.heaveneyes.check.AbstractCheck;
import dev.livingyuji.heaveneyes.model.CheckAction;
import dev.livingyuji.heaveneyes.model.CheckType;
import dev.livingyuji.heaveneyes.model.MiningProfile;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side mining analytics and statistical ore ratio analysis module.
 * Defaults to REVIEW mode.
 */
public class MiningAnalyticsCheck extends AbstractCheck {

    private int minSampleBlocks = 60;
    private double diamondRatioThreshold = 0.18;
    private double debrisRatioThreshold = 0.12;
    private boolean straightLineAlert = true;

    private final Map<UUID, Long> consecutiveOres = new ConcurrentHashMap<>();

    public MiningAnalyticsCheck(HeavenEyesPlugin plugin) {
        super(plugin, CheckType.MINING_ANALYTICS, true, 0.95, CheckAction.REVIEW);
    }

    public void configure(int minSampleBlocks, double diamondRatioThreshold, double debrisRatioThreshold, boolean straightLineAlert) {
        this.minSampleBlocks = minSampleBlocks;
        this.diamondRatioThreshold = diamondRatioThreshold;
        this.debrisRatioThreshold = debrisRatioThreshold;
        this.straightLineAlert = straightLineAlert;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!enabled) return;
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (plugin.getPunishmentEngine().isExempt(player)) return;

        Block block = event.getBlock();
        Material type = block.getType();
        UUID uuid = player.getUniqueId();

        MiningProfile profile = plugin.getPlayerDataManager().getMiningProfile(uuid);
        if (profile == null) return;

        boolean isStone = isBaseStone(type);
        boolean isDeepslate = isBaseDeepslate(type);
        boolean isDiamond = (type == Material.DIAMOND_ORE || type == Material.DEEPSLATE_DIAMOND_ORE);
        boolean isDebris = (type == Material.ANCIENT_DEBRIS);
        boolean isGold = (type == Material.GOLD_ORE || type == Material.DEEPSLATE_GOLD_ORE || type == Material.NETHER_GOLD_ORE);
        boolean isIron = (type == Material.IRON_ORE || type == Material.DEEPSLATE_IRON_ORE);
        boolean isEmerald = (type == Material.EMERALD_ORE || type == Material.DEEPSLATE_EMERALD_ORE);

        if (isStone) {
            profile.addStone(1);
            consecutiveOres.remove(uuid);
        } else if (isDeepslate) {
            profile.addDeepslate(1);
            consecutiveOres.remove(uuid);
        } else if (isDiamond) {
            profile.addDiamond(1);
            handleOreFind(player, profile, "Diamond", profile.getDiamondRatio(), diamondRatioThreshold);
        } else if (isDebris) {
            profile.addAncientDebris(1);
            handleOreFind(player, profile, "Ancient Debris", profile.getAncientDebrisRatio(), debrisRatioThreshold);
        } else if (isGold) {
            profile.addGold(1);
        } else if (isIron) {
            profile.addIron(1);
        } else if (isEmerald) {
            profile.addEmerald(1);
        }

        profile.setLastMinedTimestamp(System.currentTimeMillis());
    }

    private void handleOreFind(Player player, MiningProfile profile, String oreName, double ratio, double threshold) {
        long totalBase = profile.getTotalBaseBlocks();
        if (totalBase < minSampleBlocks) {
            return;
        }

        if (ratio > threshold) {
            double confidence = Math.min(0.98, 0.70 + (ratio * 1.5));
            Map<String, Object> details = new HashMap<>();
            details.put("ore_type", oreName);
            details.put("ratio", Math.round(ratio * 1000.0) / 1000.0);
            details.put("threshold", threshold);
            details.put("total_base_blocks", totalBase);
            details.put("diamond_count", profile.getDiamondOreMined());
            details.put("debris_count", profile.getAncientDebrisMined());

            String explanation = String.format("Statistical mining anomaly: %s ratio %.1f%% exceeds threshold %.1f%% over %d base blocks",
                    oreName, ratio * 100.0, threshold * 100.0, totalBase);
            flag(player, confidence, explanation, details);
        }
    }

    private boolean isBaseStone(Material mat) {
        return mat == Material.STONE || mat == Material.COBBLESTONE || mat == Material.ANDESITE
                || mat == Material.DIORITE || mat == Material.GRANITE || mat == Material.NETHERRACK;
    }

    private boolean isBaseDeepslate(Material mat) {
        return mat == Material.DEEPSLATE || mat == Material.COBBLED_DEEPSLATE || mat == Material.TUFF || mat == Material.BASALT;
    }

    @Override
    public void cleanup(UUID uuid) {
        super.cleanup(uuid);
        consecutiveOres.remove(uuid);
    }
}
