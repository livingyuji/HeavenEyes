package dev.livingyuji.heaveneyes.discord;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Encapsulates Discord integration settings and webhook validation.
 */
public class DiscordConfig {

    private static final Pattern WEBHOOK_URL_PATTERN = Pattern.compile(
            "^https://(?:ptb\\.|canary\\.)?(?:discord\\.com|discordapp\\.com)/api/webhooks/\\d+/[A-Za-z0-9_-]+$"
    );

    private boolean enabled = false;
    private String serverName = "Heaven Eyes Server";

    private String alertsWebhook = "";
    private String punishmentsWebhook = "";
    private String historyWebhook = "";
    private String systemLogsWebhook = "";

    private boolean alertsEnabled = true;
    private int alertsMinVl = 3;
    private int alertsCooldownSeconds = 10;
    private boolean alertsBatchRepeated = true;

    private boolean punishmentsEnabled = true;
    private boolean logWarnings = true;
    private boolean logKicks = true;
    private boolean logTemporaryBans = true;
    private boolean logPermanentBans = true;
    private boolean logUnbans = true;

    private boolean playerHistoryEnabled = true;
    private boolean systemLogsEnabled = true;

    private boolean deliveryAsync = true;
    private boolean deliveryRetryEnabled = true;
    private int deliveryMaxRetries = 5;
    private int deliveryQueueCapacity = 1000;

    public void load(FileConfiguration config) {
        if (!config.contains("discord")) {
            this.enabled = false;
            return;
        }

        this.enabled = config.getBoolean("discord.enabled", false);
        this.serverName = config.getString("discord.server-name", "Heaven Eyes Server");

        this.alertsWebhook = sanitizeWebhook(config.getString("discord.webhook-urls.alerts", ""));
        this.punishmentsWebhook = sanitizeWebhook(config.getString("discord.webhook-urls.punishments", ""));
        this.historyWebhook = sanitizeWebhook(config.getString("discord.webhook-urls.history", ""));
        this.systemLogsWebhook = sanitizeWebhook(config.getString("discord.webhook-urls.system-logs", ""));

        this.alertsEnabled = config.getBoolean("discord.alerts.enabled", true);
        this.alertsMinVl = Math.max(1, config.getInt("discord.alerts.minimum-violation-level", 3));
        this.alertsCooldownSeconds = Math.max(0, config.getInt("discord.alerts.cooldown-seconds", 10));
        this.alertsBatchRepeated = config.getBoolean("discord.alerts.batch-repeated-alerts", true);

        this.punishmentsEnabled = config.getBoolean("discord.punishments.enabled", true);
        this.logWarnings = config.getBoolean("discord.punishments.log-warnings", true);
        this.logKicks = config.getBoolean("discord.punishments.log-kicks", true);
        this.logTemporaryBans = config.getBoolean("discord.punishments.log-temporary-bans", true);
        this.logPermanentBans = config.getBoolean("discord.punishments.log-permanent-bans", true);
        this.logUnbans = config.getBoolean("discord.punishments.log-unbans", true);

        this.playerHistoryEnabled = config.getBoolean("discord.player-history.enabled", true);
        this.systemLogsEnabled = config.getBoolean("discord.system-logs.enabled", true);

        this.deliveryAsync = config.getBoolean("discord.delivery.asynchronous", true);
        this.deliveryRetryEnabled = config.getBoolean("discord.delivery.retry-enabled", true);
        this.deliveryMaxRetries = Math.max(1, config.getInt("discord.delivery.max-retries", 5));
        this.deliveryQueueCapacity = Math.max(50, config.getInt("discord.delivery.queue-capacity", 1000));
    }

    private String sanitizeWebhook(String url) {
        return url != null ? url.trim() : "";
    }

    public static boolean isValidWebhookUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        return WEBHOOK_URL_PATTERN.matcher(url.trim()).matches();
    }

    public static String maskWebhookUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return "<none>";
        }
        String trimmed = url.trim();
        int lastSlash = trimmed.lastIndexOf('/');
        if (lastSlash != -1 && lastSlash > 30) {
            String prefix = trimmed.substring(0, lastSlash);
            return prefix + "/***";
        }
        return "https://discord.com/api/webhooks/***";
    }

    public String resolveAlertsWebhook() {
        return isValidWebhookUrl(alertsWebhook) ? alertsWebhook : null;
    }

    public String resolvePunishmentsWebhook() {
        if (isValidWebhookUrl(punishmentsWebhook)) return punishmentsWebhook;
        return resolveAlertsWebhook();
    }

    public String resolveHistoryWebhook() {
        if (isValidWebhookUrl(historyWebhook)) return historyWebhook;
        return resolveAlertsWebhook();
    }

    public String resolveSystemLogsWebhook() {
        if (isValidWebhookUrl(systemLogsWebhook)) return systemLogsWebhook;
        return resolveAlertsWebhook();
    }

    // Getters
    public boolean isEnabled() { return enabled; }
    public String getServerName() { return serverName; }
    public boolean isAlertsEnabled() { return alertsEnabled; }
    public int getAlertsMinVl() { return alertsMinVl; }
    public int getAlertsCooldownSeconds() { return alertsCooldownSeconds; }
    public boolean isAlertsBatchRepeated() { return alertsBatchRepeated; }
    public boolean isPunishmentsEnabled() { return punishmentsEnabled; }
    public boolean isLogWarnings() { return logWarnings; }
    public boolean isLogKicks() { return logKicks; }
    public boolean isLogTemporaryBans() { return logTemporaryBans; }
    public boolean isLogPermanentBans() { return logPermanentBans; }
    public boolean isLogUnbans() { return logUnbans; }
    public boolean isPlayerHistoryEnabled() { return playerHistoryEnabled; }
    public boolean isSystemLogsEnabled() { return systemLogsEnabled; }
    public boolean isDeliveryAsync() { return deliveryAsync; }
    public boolean isDeliveryRetryEnabled() { return deliveryRetryEnabled; }
    public int getDeliveryMaxRetries() { return deliveryMaxRetries; }
    public int getDeliveryQueueCapacity() { return deliveryQueueCapacity; }
}
