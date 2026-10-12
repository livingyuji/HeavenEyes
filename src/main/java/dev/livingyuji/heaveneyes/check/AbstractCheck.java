package dev.livingyuji.heaveneyes.check;

import dev.livingyuji.heaveneyes.HeavenEyesPlugin;
import dev.livingyuji.heaveneyes.model.CheckAction;
import dev.livingyuji.heaveneyes.model.CheckCategory;
import dev.livingyuji.heaveneyes.model.CheckType;
import dev.livingyuji.heaveneyes.model.Violation;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base abstract check class representing an anti-cheat detection module.
 */
public abstract class AbstractCheck implements Listener {

    protected final HeavenEyesPlugin plugin;
    protected final CheckType type;
    protected boolean enabled;
    protected double minimumConfidence;
    protected CheckAction action;
    protected final Map<UUID, Integer> violationLevels = new ConcurrentHashMap<>();
    protected final Map<UUID, Long> lastViolationTimestamps = new ConcurrentHashMap<>();

    public AbstractCheck(HeavenEyesPlugin plugin, CheckType type, boolean enabled, double minimumConfidence, CheckAction action) {
        this.plugin = plugin;
        this.type = type;
        this.enabled = enabled;
        this.minimumConfidence = minimumConfidence;
        this.action = action;
    }

    public CheckType getType() {
        return type;
    }

    public String getName() {
        return type.getIdentifier();
    }

    public CheckCategory getCategory() {
        return type.getCategory();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getMinimumConfidence() {
        return minimumConfidence;
    }

    public void setMinimumConfidence(double minimumConfidence) {
        this.minimumConfidence = minimumConfidence;
    }

    public CheckAction getAction() {
        return action;
    }

    public void setAction(CheckAction action) {
        this.action = action;
    }

    public int getViolationLevel(UUID uuid) {
        return violationLevels.getOrDefault(uuid, 0);
    }

    public void setViolationLevel(UUID uuid, int vl) {
        if (vl <= 0) {
            violationLevels.remove(uuid);
        } else {
            violationLevels.put(uuid, vl);
        }
    }

    public void decayViolationLevel(UUID uuid, int decayAmount) {
        violationLevels.computeIfPresent(uuid, (id, currentVl) -> {
            int newVl = currentVl - decayAmount;
            return newVl > 0 ? newVl : null;
        });
    }

    public void cleanup(UUID uuid) {
        violationLevels.remove(uuid);
        lastViolationTimestamps.remove(uuid);
    }

    public void decayAll(int decayAmount) {
        for (UUID uuid : violationLevels.keySet()) {
            decayViolationLevel(uuid, decayAmount);
        }
    }

    /**
     * Flags a player for a detected violation.
     */
    protected void flag(Player player, double confidence, String explanation, Map<String, Object> diagnosticData) {
        if (!enabled) {
            return;
        }

        UUID uuid = player.getUniqueId();
        int newVl = violationLevels.compute(uuid, (k, current) -> (current == null ? 0 : current) + 1);
        lastViolationTimestamps.put(uuid, System.currentTimeMillis());

        Location loc = player.getLocation();
        Violation violation = new Violation(
                uuid,
                player.getName(),
                type,
                newVl,
                confidence,
                System.currentTimeMillis(),
                loc.getWorld() != null ? loc.getWorld().getName() : "world",
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                explanation,
                diagnosticData
        );

        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info(String.format("[DEBUG-FLAG] %s flagged %s (VL: %d, Conf: %.2f) - %s",
                    getName(), player.getName(), newVl, confidence, explanation));
        }

        plugin.getPunishmentEngine().handleViolation(player, this, violation);
    }
}
