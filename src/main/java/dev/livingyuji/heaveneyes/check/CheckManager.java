package dev.livingyuji.heaveneyes.check;

import dev.livingyuji.heaveneyes.HeavenEyesPlugin;
import dev.livingyuji.heaveneyes.check.combat.*;
import dev.livingyuji.heaveneyes.check.esp.ContainerEspCheck;
import dev.livingyuji.heaveneyes.check.esp.FreecamCheck;
import dev.livingyuji.heaveneyes.check.mining.MiningAnalyticsCheck;
import dev.livingyuji.heaveneyes.check.movement.*;
import dev.livingyuji.heaveneyes.model.CheckAction;
import dev.livingyuji.heaveneyes.model.CheckType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages registration, configuration, and lifecycle of all Heaven Eyes checks.
 */
public class CheckManager {

    private final HeavenEyesPlugin plugin;
    private final Map<CheckType, AbstractCheck> checks = new ConcurrentHashMap<>();

    public CheckManager(HeavenEyesPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerAll() {
        register(new SpeedCheck(plugin));
        register(new FlyCheck(plugin));
        register(new NoFallCheck(plugin));
        register(new TimerCheck(plugin));
        register(new StepCheck(plugin));
        register(new VelocityCheck(plugin));

        register(new ReachCheck(plugin));
        register(new KillAuraCheck(plugin));
        register(new FastClickCheck(plugin));
        register(new AimSnapCheck(plugin));
        register(new CriticalsCheck(plugin));

        register(new MiningAnalyticsCheck(plugin));

        register(new ContainerEspCheck(plugin));
        register(new FreecamCheck(plugin));

        for (AbstractCheck check : checks.values()) {
            Bukkit.getPluginManager().registerEvents(check, plugin);
        }
    }

    public void register(AbstractCheck check) {
        checks.put(check.getType(), check);
    }

    public AbstractCheck getCheck(CheckType type) {
        return checks.get(type);
    }

    public AbstractCheck getCheck(String name) {
        CheckType type = CheckType.fromString(name);
        return type != null ? checks.get(type) : null;
    }

    public Collection<AbstractCheck> getAllChecks() {
        return Collections.unmodifiableCollection(checks.values());
    }

    public int getEnabledCount() {
        int count = 0;
        for (AbstractCheck check : checks.values()) {
            if (check.isEnabled()) count++;
        }
        return count;
    }

    public void loadConfigurations(FileConfiguration config) {
        // Global category actions
        String movActionStr = config.getString("checks.movement.automatic-action", "WARN_THEN_TEMPBAN");
        CheckAction movAction = CheckAction.fromString(movActionStr, CheckAction.WARN_THEN_TEMPBAN);
        double movMinConf = config.getDouble("checks.movement.minimum-confidence", 0.90);
        boolean movEnabled = config.getBoolean("checks.movement.enabled", true);

        String combatActionStr = config.getString("checks.combat.automatic-action", "WARN_THEN_TEMPBAN");
        CheckAction combatAction = CheckAction.fromString(combatActionStr, CheckAction.WARN_THEN_TEMPBAN);
        double combatMinConf = config.getDouble("checks.combat.minimum-confidence", 0.95);
        boolean combatEnabled = config.getBoolean("checks.combat.enabled", true);

        String miningActionStr = config.getString("checks.mining.automatic-action", "REVIEW");
        CheckAction miningAction = CheckAction.fromString(miningActionStr, CheckAction.REVIEW);
        double miningMinConf = config.getDouble("checks.mining.minimum-confidence", 0.95);
        boolean miningEnabled = config.getBoolean("checks.mining.enabled", true);

        String espActionStr = config.getString("checks.esp.automatic-action", "REVIEW");
        CheckAction espAction = CheckAction.fromString(espActionStr, CheckAction.REVIEW);
        boolean espEnabled = config.getBoolean("checks.esp.enabled", true);

        String freecamActionStr = config.getString("checks.freecam.automatic-action", "REVIEW");
        CheckAction freecamAction = CheckAction.fromString(freecamActionStr, CheckAction.REVIEW);
        boolean freecamEnabled = config.getBoolean("checks.freecam.enabled", true);

        // Apply to movement checks
        for (AbstractCheck check : checks.values()) {
            switch (check.getCategory()) {
                case MOVEMENT -> {
                    check.setEnabled(movEnabled && config.getBoolean("checks.movement." + check.getName().toLowerCase() + ".enabled", true));
                    check.setAction(movAction);
                    check.setMinimumConfidence(movMinConf);
                }
                case COMBAT -> {
                    check.setEnabled(combatEnabled && config.getBoolean("checks.combat." + check.getName().toLowerCase() + ".enabled", true));
                    check.setAction(combatAction);
                    check.setMinimumConfidence(combatMinConf);
                }
                case MINING -> {
                    check.setEnabled(miningEnabled);
                    check.setAction(miningAction);
                    check.setMinimumConfidence(miningMinConf);
                }
                case ESP -> {
                    check.setEnabled(espEnabled);
                    check.setAction(espAction);
                }
                case FREECAM -> {
                    check.setEnabled(freecamEnabled);
                    check.setAction(freecamAction);
                }
            }
        }
    }

    public void decayAllViolations(int decayAmount) {
        for (AbstractCheck check : checks.values()) {
            check.decayAll(decayAmount);
        }
    }

    public void cleanupPlayer(UUID uuid) {
        for (AbstractCheck check : checks.values()) {
            check.cleanup(uuid);
        }
    }
}
