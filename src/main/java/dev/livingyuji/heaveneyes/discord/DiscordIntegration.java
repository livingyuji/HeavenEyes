package dev.livingyuji.heaveneyes.discord;

import dev.livingyuji.heaveneyes.HeavenEyesPlugin;
import dev.livingyuji.heaveneyes.check.AbstractCheck;
import dev.livingyuji.heaveneyes.model.BanRecord;
import dev.livingyuji.heaveneyes.model.Violation;
import dev.livingyuji.heaveneyes.model.WarningRecord;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * Primary coordinator and API gateway for Heaven Eyes Discord Webhook integration.
 */
public class DiscordIntegration {

    private final HeavenEyesPlugin plugin;
    private final Logger logger;
    private DiscordConfig config;
    private DiscordWebhookClient client;
    private DiscordDeliveryQueue queue;

    public DiscordIntegration(HeavenEyesPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public synchronized void initialize() {
        this.config = new DiscordConfig();
        this.config.load(plugin.getConfig());

        if (!config.isEnabled()) {
            if (plugin.isDebugEnabled()) {
                logger.info("[Discord] Discord integration is disabled in config.yml.");
            }
            return;
        }

        this.client = new DiscordWebhookClient();
        this.queue = new DiscordDeliveryQueue(client, config, logger);

        logger.info("[Discord] Discord Webhook integration initialized successfully.");
        if (config.isSystemLogsEnabled()) {
            sendSystemLog("Server Online", "Heaven Eyes v1.0.0 enabled on " + config.getServerName(), false);
        }
    }

    public synchronized void reload() {
        shutdown();
        initialize();
    }

    public synchronized void shutdown() {
        if (queue != null) {
            if (config != null && config.isSystemLogsEnabled()) {
                sendSystemLog("Server Stopping", "Heaven Eyes v1.0.0 shutting down on " + config.getServerName(), false);
            }
            queue.shutdown(3000L);
            queue = null;
        }
    }

    public boolean isEnabled() {
        return config != null && config.isEnabled() && queue != null;
    }

    public void sendAlert(Player player, AbstractCheck check, Violation violation, String incidentId, String actionStatus) {
        if (!isEnabled() || !config.isAlertsEnabled()) {
            return;
        }

        // Check minimum violation level
        if (violation.getViolationLevel() < config.getAlertsMinVl()) {
            return;
        }

        // Duplicate alert suppression
        String key = player.getUniqueId() + ":" + check.getType().name();
        if (queue.shouldSuppressAlert(key, config.getAlertsCooldownSeconds())) {
            return;
        }

        String webhook = config.resolveAlertsWebhook();
        if (webhook == null) return;

        DiscordEmbed embed = DiscordMessageBuilder.buildAlertEmbed(
                player.getName(),
                player.getUniqueId(),
                check,
                violation,
                incidentId,
                actionStatus,
                config.getServerName()
        );

        queue.enqueue(webhook, DiscordMessageBuilder.wrapPayload(embed));
    }

    public void sendWarning(Player player, WarningRecord warning, int maxWarnings) {
        if (!isEnabled() || !config.isPunishmentsEnabled() || !config.isLogWarnings()) {
            return;
        }

        String webhook = config.resolvePunishmentsWebhook();
        if (webhook == null) return;

        DiscordEmbed embed = DiscordMessageBuilder.buildWarningEmbed(
                player.getName(),
                player.getUniqueId(),
                warning,
                maxWarnings,
                config.getServerName()
        );

        queue.enqueue(webhook, DiscordMessageBuilder.wrapPayload(embed));
    }

    public void sendKick(String playerName, UUID playerUuid, String reason, String incidentId, String actor) {
        if (!isEnabled() || !config.isPunishmentsEnabled() || !config.isLogKicks()) {
            return;
        }

        String webhook = config.resolvePunishmentsWebhook();
        if (webhook == null) return;

        DiscordEmbed embed = DiscordMessageBuilder.buildKickEmbed(
                playerName,
                playerUuid,
                reason,
                incidentId,
                actor,
                config.getServerName()
        );

        queue.enqueue(webhook, DiscordMessageBuilder.wrapPayload(embed));
    }

    public void sendBan(BanRecord ban) {
        if (!isEnabled() || !config.isPunishmentsEnabled()) {
            return;
        }

        boolean isPermanent = ban.getType() == BanRecord.Type.PERMANENT;
        if (isPermanent && !config.isLogPermanentBans()) return;
        if (!isPermanent && !config.isLogTemporaryBans()) return;

        String webhook = config.resolvePunishmentsWebhook();
        if (webhook == null) return;

        DiscordEmbed embed = DiscordMessageBuilder.buildBanEmbed(ban, config.getServerName());
        queue.enqueue(webhook, DiscordMessageBuilder.wrapPayload(embed));
    }

    public void sendUnban(BanRecord ban, String actor, String reason) {
        if (!isEnabled() || !config.isPunishmentsEnabled() || !config.isLogUnbans()) {
            return;
        }

        String webhook = config.resolvePunishmentsWebhook();
        if (webhook == null) return;

        DiscordEmbed embed = DiscordMessageBuilder.buildUnbanEmbed(ban, actor, reason, config.getServerName());
        queue.enqueue(webhook, DiscordMessageBuilder.wrapPayload(embed));
    }

    public void sendPlayerHistory(UUID uuid, String playerName, String eventType, String summary) {
        if (!isEnabled() || !config.isPlayerHistoryEnabled()) {
            return;
        }

        String webhook = config.resolveHistoryWebhook();
        if (webhook == null) return;

        DiscordEmbed embed = DiscordMessageBuilder.buildPlayerHistoryEmbed(uuid, playerName, eventType, summary, config.getServerName());
        queue.enqueue(webhook, DiscordMessageBuilder.wrapPayload(embed));
    }

    public void sendSystemLog(String title, String message, boolean isError) {
        if (!isEnabled() || !config.isSystemLogsEnabled()) {
            return;
        }

        String webhook = config.resolveSystemLogsWebhook();
        if (webhook == null) return;

        DiscordEmbed embed = DiscordMessageBuilder.buildSystemLogEmbed(title, message, isError, config.getServerName());
        queue.enqueue(webhook, DiscordMessageBuilder.wrapPayload(embed));
    }

    public DiscordConfig getConfig() {
        return config;
    }
}
