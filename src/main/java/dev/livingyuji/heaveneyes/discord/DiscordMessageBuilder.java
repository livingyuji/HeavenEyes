package dev.livingyuji.heaveneyes.discord;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.livingyuji.heaveneyes.check.AbstractCheck;
import dev.livingyuji.heaveneyes.model.BanRecord;
import dev.livingyuji.heaveneyes.model.Violation;
import dev.livingyuji.heaveneyes.model.WarningRecord;
import dev.livingyuji.heaveneyes.util.DurationParser;

import java.time.Instant;
import java.util.UUID;

/**
 * Constructs structured Discord webhook payloads and rich embeds.
 */
public class DiscordMessageBuilder {

    private static final String BOT_NAME = "Heaven Eyes Anti-Cheat";
    private static final String AVATAR_URL = "https://raw.githubusercontent.com/livingyuji/Heaven_eyes/main/assets/icon.png";
    private static final String AUTHOR_TITLE = "Heaven Eyes Anti-Cheat • LivingYuji";

    public static String wrapPayload(DiscordEmbed embed) {
        JsonObject payload = new JsonObject();
        payload.addProperty("username", BOT_NAME);
        payload.addProperty("avatar_url", AVATAR_URL);

        // Explicitly disable all user/role/everyone pings
        JsonObject allowedMentions = new JsonObject();
        allowedMentions.add("parse", new JsonArray());
        payload.add("allowed_mentions", allowedMentions);

        JsonArray embeds = new JsonArray();
        embeds.add(embed.toJson());
        payload.add("embeds", embeds);

        return payload.toString();
    }

    public static DiscordEmbed buildAlertEmbed(String playerName, UUID playerUuid, AbstractCheck check,
                                              Violation violation, String incidentId, String actionStatus, String serverName) {
        DiscordEmbed embed = new DiscordEmbed();
        embed.setAuthor(AUTHOR_TITLE, AVATAR_URL);
        embed.setTitle("🚨 Anti-Cheat Violation Detected");
        embed.setColor(0xE74C3C); // Crimson Red
        embed.setTimestamp(Instant.ofEpochMilli(violation.getTimestamp()));
        embed.setFooter(serverName + " • Heaven Eyes v1.0.0", null);
        embed.setThumbnail("https://mc-heads.net/avatar/" + playerUuid + "/100");

        embed.addField("Player", "`" + playerName + "`\n(" + playerUuid + ")", true);
        embed.addField("Check", "**" + check.getName() + "** (" + check.getCategory().getDisplayName() + ")", true);
        embed.addField("Severity", "VL **" + violation.getViolationLevel() + "** | Conf: **" + (int)(violation.getConfidence() * 100) + "%**", true);

        embed.addField("Server", serverName, true);
        embed.addField("Action / Status", actionStatus != null ? actionStatus : "Monitored", true);
        embed.addField("Evidence ID", "`" + (incidentId != null ? incidentId : "Pending") + "`", true);

        embed.addField("Detection Summary", violation.getExplanation(), false);

        if (!violation.getDiagnosticData().isEmpty()) {
            StringBuilder sb = new StringBuilder("```json\n");
            violation.getDiagnosticData().forEach((k, v) -> sb.append(k).append(": ").append(v).append("\n"));
            sb.append("```");
            embed.addField("Diagnostic Metrics", sb.toString(), false);
        }

        return embed;
    }

    public static DiscordEmbed buildWarningEmbed(String playerName, UUID playerUuid, WarningRecord warning,
                                                int maxWarnings, String serverName) {
        DiscordEmbed embed = new DiscordEmbed();
        embed.setAuthor(AUTHOR_TITLE, AVATAR_URL);
        embed.setTitle("⚠️ Warning Issued (" + warning.getCount() + "/" + maxWarnings + ")");
        embed.setColor(0xF1C40F); // Gold
        embed.setTimestamp(Instant.ofEpochMilli(warning.getTimestamp()));
        embed.setFooter(serverName + " • Heaven Eyes v1.0.0", null);
        embed.setThumbnail("https://mc-heads.net/avatar/" + playerUuid + "/100");

        embed.addField("Player", "`" + playerName + "`\n(" + playerUuid + ")", true);
        embed.addField("Check", warning.getCheckType() != null ? warning.getCheckType().getIdentifier() : "Anti-Cheat", true);
        embed.addField("Warning Count", warning.getCount() + " of " + maxWarnings, true);

        embed.addField("Reason", warning.getReason(), false);
        embed.addField("Evidence ID", "`" + warning.getEvidenceId() + "`", true);
        embed.addField("Server", serverName, true);

        return embed;
    }

    public static DiscordEmbed buildKickEmbed(String playerName, UUID playerUuid, String reason,
                                             String incidentId, String actor, String serverName) {
        DiscordEmbed embed = new DiscordEmbed();
        embed.setAuthor(AUTHOR_TITLE, AVATAR_URL);
        embed.setTitle("👢 Player Kicked");
        embed.setColor(0xE67E22); // Orange
        embed.setTimestamp(Instant.now());
        embed.setFooter(serverName + " • Heaven Eyes v1.0.0", null);
        embed.setThumbnail("https://mc-heads.net/avatar/" + playerUuid + "/100");

        embed.addField("Player", "`" + playerName + "`\n(" + playerUuid + ")", true);
        embed.addField("Enforced By", actor != null ? actor : "HeavenEyes", true);
        embed.addField("Evidence ID", "`" + (incidentId != null ? incidentId : "N/A") + "`", true);

        embed.addField("Reason", reason, false);
        embed.addField("Server", serverName, true);

        return embed;
    }

    public static DiscordEmbed buildBanEmbed(BanRecord ban, String serverName) {
        boolean isPermanent = ban.getType() == BanRecord.Type.PERMANENT;
        DiscordEmbed embed = new DiscordEmbed();
        embed.setAuthor(AUTHOR_TITLE, AVATAR_URL);
        embed.setTitle(isPermanent ? "🔨 Permanent Ban Applied" : "⏳ Temporary Ban Applied");
        embed.setColor(0x992D22); // Dark Red
        embed.setTimestamp(Instant.ofEpochMilli(ban.getCreatedAt()));
        embed.setFooter(serverName + " • Heaven Eyes v1.0.0", null);
        embed.setThumbnail("https://mc-heads.net/avatar/" + ban.getPlayerUuid() + "/100");

        embed.addField("Player", "`" + ban.getPlayerName() + "`\n(" + ban.getPlayerUuid() + ")", true);
        embed.addField("Ban ID", "`" + ban.getBanId() + "`", true);
        embed.addField("Source", ban.getSource().name(), true);

        if (!isPermanent) {
            long durationMillis = ban.getExpiresAt() - ban.getCreatedAt();
            embed.addField("Duration", DurationParser.format(durationMillis), true);
            embed.addField("Expires In", DurationParser.format(ban.getRemainingMillis()), true);
        } else {
            embed.addField("Duration", "Permanent", true);
        }

        embed.addField("Enforced By", ban.getActor(), true);
        embed.addField("Reason", ban.getReason(), false);

        if (ban.getEvidenceId() != null) {
            embed.addField("Evidence ID", "`" + ban.getEvidenceId() + "`", true);
        }
        embed.addField("Server", serverName, true);

        return embed;
    }

    public static DiscordEmbed buildUnbanEmbed(BanRecord ban, String actor, String reason, String serverName) {
        DiscordEmbed embed = new DiscordEmbed();
        embed.setAuthor(AUTHOR_TITLE, AVATAR_URL);
        embed.setTitle("⚖️ Ban Pardoned / Player Unbanned");
        embed.setColor(0x2ECC71); // Green
        embed.setTimestamp(Instant.now());
        embed.setFooter(serverName + " • Heaven Eyes v1.0.0", null);
        embed.setThumbnail("https://mc-heads.net/avatar/" + ban.getPlayerUuid() + "/100");

        embed.addField("Player", "`" + ban.getPlayerName() + "`\n(" + ban.getPlayerUuid() + ")", true);
        embed.addField("Ban ID", "`" + ban.getBanId() + "`", true);
        embed.addField("Pardoned By", actor, true);

        embed.addField("Original Reason", ban.getReason(), false);
        embed.addField("Pardon Reason", reason != null ? reason : "Administrative pardon", false);
        embed.addField("Server", serverName, true);

        return embed;
    }

    public static DiscordEmbed buildPlayerHistoryEmbed(UUID uuid, String playerName, String eventType,
                                                      String summary, String serverName) {
        DiscordEmbed embed = new DiscordEmbed();
        embed.setAuthor(AUTHOR_TITLE, AVATAR_URL);
        embed.setTitle("📜 Player Audit Record: " + eventType);
        embed.setColor(0x3498DB); // Blue
        embed.setTimestamp(Instant.now());
        embed.setFooter(serverName + " • Heaven Eyes v1.0.0", null);
        embed.setThumbnail("https://mc-heads.net/avatar/" + uuid + "/100");

        embed.addField("Player", "`" + playerName + "`\n(" + uuid + ")", true);
        embed.addField("Event Type", eventType, true);
        embed.addField("Server", serverName, true);
        embed.addField("Summary", summary, false);

        return embed;
    }

    public static DiscordEmbed buildSystemLogEmbed(String title, String message, boolean isError, String serverName) {
        DiscordEmbed embed = new DiscordEmbed();
        embed.setAuthor(AUTHOR_TITLE, AVATAR_URL);
        embed.setTitle(isError ? "⚠️ System Alert: " + title : "ℹ️ System: " + title);
        embed.setColor(isError ? 0xC0392B : 0x1ABC9C); // Red for error, Teal for info
        embed.setTimestamp(Instant.now());
        embed.setFooter(serverName + " • Heaven Eyes v1.0.0", null);

        embed.addField("Event", title, true);
        embed.addField("Server", serverName, true);
        embed.addField("Message", message, false);

        return embed;
    }
}
