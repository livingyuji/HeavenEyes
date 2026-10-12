package dev.livingyuji.heaveneyes;

import dev.livingyuji.heaveneyes.alerts.AlertManager;
import dev.livingyuji.heaveneyes.check.CheckManager;
import dev.livingyuji.heaveneyes.command.HeavenEyesCommand;
import dev.livingyuji.heaveneyes.config.*;
import dev.livingyuji.heaveneyes.evidence.EvidenceManager;
import dev.livingyuji.heaveneyes.integration.LuckPermsIntegration;
import dev.livingyuji.heaveneyes.punishment.BanManager;
import dev.livingyuji.heaveneyes.punishment.PunishmentEngine;
import dev.livingyuji.heaveneyes.punishment.WarningManager;
import dev.livingyuji.heaveneyes.storage.*;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;

/**
 * Heaven Eyes — Advanced Minecraft Anti-Cheat Plugin.
 * Target Platform: Paper 1.21.x
 * Author: LivingYuji
 */
public class HeavenEyesPlugin extends JavaPlugin implements Listener {

    private static HeavenEyesPlugin instance;

    private PluginConfig pluginConfig;
    private PunishmentsConfig punishmentsConfig;
    private MessagesConfig messagesConfig;
    private StaffConfig staffConfig;

    private CheckManager checkManager;
    private PunishmentEngine punishmentEngine;
    private BanManager banManager;
    private WarningManager warningManager;
    private EvidenceManager evidenceManager;
    private PlayerDataManager playerDataManager;
    private AuditLogger auditLogger;
    private BackupManager backupManager;
    private ExportManager exportManager;
    private CleanupManager cleanupManager;
    private AlertManager alertManager;
    private LuckPermsIntegration luckPermsIntegration;

    private BukkitTask decayTask;

    @Override
    public void onEnable() {
        instance = this;
        long startTime = System.currentTimeMillis();

        // Save default configuration assets
        saveDefaultConfig();
        saveResourceIfNotExists("checks.yml");
        saveResourceIfNotExists("punishments.yml");
        saveResourceIfNotExists("messages.yml");
        saveResourceIfNotExists("staff.yml");

        // Initialize configuration handlers
        this.pluginConfig = new PluginConfig();
        this.punishmentsConfig = new PunishmentsConfig();
        this.messagesConfig = new MessagesConfig();
        this.staffConfig = new StaffConfig(this);

        loadConfigurations();

        // Initialize core storage and services
        this.auditLogger = new AuditLogger(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.playerDataManager.initialize();

        this.evidenceManager = new EvidenceManager(this);
        this.evidenceManager.initialize();

        this.backupManager = new BackupManager(this);
        this.backupManager.initialize();

        this.exportManager = new ExportManager(this);
        this.exportManager.initialize();

        this.cleanupManager = new CleanupManager(this);
        this.alertManager = new AlertManager(this);
        this.warningManager = new WarningManager(this);

        this.banManager = new BanManager(this);
        this.banManager.initialize();

        this.punishmentEngine = new PunishmentEngine(this);
        this.luckPermsIntegration = new LuckPermsIntegration(this);

        // Register check systems
        this.checkManager = new CheckManager(this);
        this.checkManager.registerAll();

        File checksFile = new File(getDataFolder(), "checks.yml");
        this.checkManager.loadConfigurations(YamlConfiguration.loadConfiguration(checksFile));

        // Register player lifecycle listeners
        Bukkit.getPluginManager().registerEvents(this, this);

        // Register commands
        HeavenEyesCommand commandExecutor = new HeavenEyesCommand(this);
        PluginCommand heCmd = getCommand("he");
        if (heCmd != null) {
            heCmd.setExecutor(commandExecutor);
            heCmd.setTabCompleter(commandExecutor);
        }

        // Start decay repeating task
        startDecayTask();

        long loadTime = System.currentTimeMillis() - startTime;
        getLogger().info("=================================================");
        getLogger().info("  Heaven Eyes Anti-Cheat v1.0.0 Enabled");
        getLogger().info("  Author: LivingYuji");
        getLogger().info("  Enabled Checks: " + checkManager.getEnabledCount() + "/" + checkManager.getAllChecks().size());
        getLogger().info("  LuckPerms Integration: " + (luckPermsIntegration.isAvailable() ? "Connected" : "Unavailable (Soft-Depend)"));
        getLogger().info("  Initialization completed in " + loadTime + "ms");
        getLogger().info("=================================================");

        auditLogger.logAction("PLUGIN_ENABLED version=1.0.0 author=LivingYuji");
    }

    @Override
    public void onDisable() {
        if (decayTask != null) {
            decayTask.cancel();
            decayTask = null;
        }

        if (banManager != null) {
            banManager.saveBans();
        }

        if (playerDataManager != null) {
            playerDataManager.saveAllSync();
        }

        if (auditLogger != null) {
            auditLogger.logAction("PLUGIN_DISABLED");
        }

        getLogger().info("Heaven Eyes Anti-Cheat disabled safely. Records persisted.");
        instance = null;
    }

    public void loadConfigurations() {
        reloadConfig();
        pluginConfig.load(getConfig());

        File punFile = new File(getDataFolder(), "punishments.yml");
        punishmentsConfig.load(YamlConfiguration.loadConfiguration(punFile));

        File msgFile = new File(getDataFolder(), "messages.yml");
        messagesConfig.load(YamlConfiguration.loadConfiguration(msgFile));

        staffConfig.load();
    }

    public synchronized void reloadAll() {
        loadConfigurations();

        File checksFile = new File(getDataFolder(), "checks.yml");
        checkManager.loadConfigurations(YamlConfiguration.loadConfiguration(checksFile));

        banManager.loadBans();
        auditLogger.logAction("CONFIGURATION_RELOADED");
    }

    private void startDecayTask() {
        int interval = pluginConfig.getTickInterval();
        this.decayTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            checkManager.decayAllViolations(1);
        }, interval, interval);
    }

    private void saveResourceIfNotExists(String resourcePath) {
        File file = new File(getDataFolder(), resourcePath);
        if (!file.exists()) {
            saveResource(resourcePath, false);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        playerDataManager.getPlayerProfile(player.getUniqueId(), player.getName());
        playerDataManager.getMiningProfile(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        checkManager.cleanupPlayer(player.getUniqueId());
        punishmentEngine.cleanup(player.getUniqueId());
        warningManager.cleanup(player.getUniqueId());
        playerDataManager.cleanup(player.getUniqueId());
    }

    public boolean isDebugEnabled() {
        return pluginConfig.isDebug();
    }

    public static HeavenEyesPlugin getInstance() { return instance; }
    public PluginConfig getPluginConfig() { return pluginConfig; }
    public PunishmentsConfig getPunishmentsConfig() { return punishmentsConfig; }
    public MessagesConfig getMessagesConfig() { return messagesConfig; }
    public StaffConfig getStaffConfig() { return staffConfig; }
    public CheckManager getCheckManager() { return checkManager; }
    public PunishmentEngine getPunishmentEngine() { return punishmentEngine; }
    public BanManager getBanManager() { return banManager; }
    public WarningManager getWarningManager() { return warningManager; }
    public EvidenceManager getEvidenceManager() { return evidenceManager; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public AuditLogger getAuditLogger() { return auditLogger; }
    public BackupManager getBackupManager() { return backupManager; }
    public ExportManager getExportManager() { return exportManager; }
    public CleanupManager getCleanupManager() { return cleanupManager; }
    public AlertManager getAlertManager() { return alertManager; }
    public LuckPermsIntegration getLuckPermsIntegration() { return luckPermsIntegration; }
}
