package dev.livingyuji.heaveneyes.discord;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.livingyuji.heaveneyes.check.AbstractCheck;
import dev.livingyuji.heaveneyes.model.BanRecord;
import dev.livingyuji.heaveneyes.model.CheckAction;
import dev.livingyuji.heaveneyes.model.CheckType;
import dev.livingyuji.heaveneyes.model.Violation;
import dev.livingyuji.heaveneyes.model.WarningRecord;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DiscordMessageBuilderTest {

    @Test
    public void testWrapPayloadStructure() {
        DiscordEmbed embed = new DiscordEmbed().setTitle("Test");
        String payloadJson = DiscordMessageBuilder.wrapPayload(embed);

        JsonObject payload = JsonParser.parseString(payloadJson).getAsJsonObject();
        assertEquals("Heaven Eyes Anti-Cheat", payload.get("username").getAsString());
        assertTrue(payload.has("avatar_url"));

        // Verify allowed_mentions is empty to prevent mention abuse
        assertTrue(payload.has("allowed_mentions"));
        JsonObject allowedMentions = payload.getAsJsonObject("allowed_mentions");
        assertTrue(allowedMentions.has("parse"));
        JsonArray parseArray = allowedMentions.getAsJsonArray("parse");
        assertEquals(0, parseArray.size(), "Parse array must be empty to avoid ping abuse");

        assertTrue(payload.has("embeds"));
        JsonArray embeds = payload.getAsJsonArray("embeds");
        assertEquals(1, embeds.size());
    }

    @Test
    public void testBuildAlertEmbed() {
        UUID uuid = UUID.randomUUID();
        AbstractCheck check = new AbstractCheck(null, CheckType.SPEED, true, 0.8, CheckAction.WARN_THEN_TEMPBAN) {};
        Violation violation = new Violation(
                uuid, "Steve", CheckType.SPEED, 5, 0.95,
                System.currentTimeMillis(), "world", 10.0, 64.0, 20.0,
                "Horizontal delta exceeded threshold (0.62 > 0.36)",
                Map.of("deltaXZ", 0.62, "maxAllowed", 0.36)
        );

        DiscordEmbed embed = DiscordMessageBuilder.buildAlertEmbed(
                "Steve", uuid, check, violation, "INC-SPEED-001", "Cancelled Movement", "Survival-1"
        );

        assertEquals("🚨 Anti-Cheat Violation Detected", embed.getTitle());
        assertEquals(0xE74C3C, embed.getColor());
        assertFalse(embed.getFields().isEmpty());

        boolean hasPlayerField = embed.getFields().stream().anyMatch(f -> f.name.equals("Player") && f.value.contains("Steve"));
        boolean hasCheckField = embed.getFields().stream().anyMatch(f -> f.name.equals("Check") && f.value.contains("Speed"));
        boolean hasSeverityField = embed.getFields().stream().anyMatch(f -> f.name.equals("Severity") && f.value.contains("VL **5**"));
        boolean hasEvidenceField = embed.getFields().stream().anyMatch(f -> f.name.equals("Evidence ID") && f.value.contains("INC-SPEED-001"));

        assertTrue(hasPlayerField, "Alert embed must contain Player field");
        assertTrue(hasCheckField, "Alert embed must contain Check field");
        assertTrue(hasSeverityField, "Alert embed must contain Severity field");
        assertTrue(hasEvidenceField, "Alert embed must contain Evidence ID field");
    }

    @Test
    public void testBuildWarningEmbed() {
        UUID uuid = UUID.randomUUID();
        WarningRecord warning = new WarningRecord(uuid, 2, CheckType.FLIGHT, System.currentTimeMillis(), "EVI-FLIGHT-02", "Suspicious air hover");

        DiscordEmbed embed = DiscordMessageBuilder.buildWarningEmbed("Alex", uuid, warning, 3, "SkyBlock-1");

        assertTrue(embed.getTitle().contains("2/3"), "Warning embed title must display warning count");
        assertEquals(0xF1C40F, embed.getColor());
        assertTrue(embed.getFields().stream().anyMatch(f -> f.name.equals("Reason") && f.value.contains("Suspicious air hover")));
    }

    @Test
    public void testBuildKickEmbed() {
        UUID uuid = UUID.randomUUID();
        DiscordEmbed embed = DiscordMessageBuilder.buildKickEmbed("BadActor", uuid, "Timer speedup anomaly", "EVI-TIMER-01", "HeavenEyes", "Lobby");

        assertEquals("👢 Player Kicked", embed.getTitle());
        assertEquals(0xE67E22, embed.getColor());
        assertTrue(embed.getFields().stream().anyMatch(f -> f.name.equals("Reason") && f.value.contains("Timer speedup anomaly")));
    }

    @Test
    public void testBuildBanEmbedPermanentAndTemporary() {
        UUID uuid = UUID.randomUUID();
        long now = System.currentTimeMillis();

        // Permanent ban
        BanRecord permBan = new BanRecord(
                "BAN-PERM-01", uuid, "Cheater1", BanRecord.Type.PERMANENT,
                "Unfair Advantage (KillAura)", BanRecord.Source.AUTOMATIC, CheckType.KILL_AURA,
                now, 0, "HeavenEyes", "EVI-KA-01"
        );
        DiscordEmbed permEmbed = DiscordMessageBuilder.buildBanEmbed(permBan, "Survival");
        assertTrue(permEmbed.getTitle().contains("Permanent"));
        assertTrue(permEmbed.getFields().stream().anyMatch(f -> f.name.equals("Duration") && f.value.equals("Permanent")));

        // Temporary ban
        BanRecord tempBan = new BanRecord(
                "BAN-TEMP-01", uuid, "Cheater2", BanRecord.Type.TEMPORARY,
                "Suspicious Reach", BanRecord.Source.MANUAL, CheckType.REACH,
                now, now + 86400000L, "StaffAdmin", "EVI-REACH-01"
        );
        DiscordEmbed tempEmbed = DiscordMessageBuilder.buildBanEmbed(tempBan, "Survival");
        assertTrue(tempEmbed.getTitle().contains("Temporary"));
        assertTrue(tempEmbed.getFields().stream().anyMatch(f -> f.name.equals("Duration") && !f.value.equals("Permanent")));
    }

    @Test
    public void testBuildUnbanEmbed() {
        UUID uuid = UUID.randomUUID();
        BanRecord ban = new BanRecord(
                "BAN-REV-01", uuid, "AppealedPlayer", BanRecord.Type.TEMPORARY,
                "Reach violation", BanRecord.Source.MANUAL, CheckType.REACH,
                System.currentTimeMillis() - 10000, System.currentTimeMillis() + 86400000L, "Mod", "EVI-01"
        );
        DiscordEmbed embed = DiscordMessageBuilder.buildUnbanEmbed(ban, "HeadAdmin", "Appeal accepted on ticket #44", "Survival");

        assertTrue(embed.getTitle().contains("Unbanned"));
        assertTrue(embed.getFields().stream().anyMatch(f -> f.name.equals("Pardoned By") && f.value.equals("HeadAdmin")));
        assertTrue(embed.getFields().stream().anyMatch(f -> f.name.equals("Pardon Reason") && f.value.contains("Appeal accepted")));
    }

    @Test
    public void testBuildPlayerHistoryEmbed() {
        UUID uuid = UUID.randomUUID();
        DiscordEmbed embed = DiscordMessageBuilder.buildPlayerHistoryEmbed(uuid, "Steve", "Flag History", "Recorded 3 violations today", "Hub");

        assertTrue(embed.getTitle().contains("Player Audit Record"));
        assertTrue(embed.getFields().stream().anyMatch(f -> f.name.equals("Event Type") && f.value.equals("Flag History")));
    }

    @Test
    public void testBuildSystemLogEmbed() {
        DiscordEmbed infoEmbed = DiscordMessageBuilder.buildSystemLogEmbed("Started", "System operational", false, "Hub");
        assertTrue(infoEmbed.getTitle().contains("ℹ️ System: Started"));
        assertEquals(0x1ABC9C, infoEmbed.getColor());

        DiscordEmbed errorEmbed = DiscordMessageBuilder.buildSystemLogEmbed("Webhook Error", "Delivery failed", true, "Hub");
        assertTrue(errorEmbed.getTitle().contains("⚠️ System Alert: Webhook Error"));
        assertEquals(0xC0392B, errorEmbed.getColor());
    }
}
