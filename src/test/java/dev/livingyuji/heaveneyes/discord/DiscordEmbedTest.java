package dev.livingyuji.heaveneyes.discord;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class DiscordEmbedTest {

    @Test
    public void testSanitizeMentions() {
        String unsafe = "Alert! @everyone check this out: @here and ping <@123456789>";
        String safe = DiscordEmbed.sanitize(unsafe, 256);

        assertFalse(safe.contains("@everyone"), "Should not contain unescaped @everyone");
        assertFalse(safe.contains("@here"), "Should not contain unescaped @here");
        assertFalse(safe.contains("<@12"), "Should not contain unescaped user ping");
        assertTrue(safe.contains("@\u200Beveryone"));
        assertTrue(safe.contains("@\u200Bhere"));
        assertTrue(safe.contains("<@\u200B123456789>"));
    }

    @Test
    public void testSanitizeTruncation() {
        String longString = "A".repeat(300);
        String truncated = DiscordEmbed.sanitize(longString, 50);

        assertEquals(50, truncated.length());
        assertTrue(truncated.endsWith("..."));
    }

    @Test
    public void testSanitizeNullAndEmpty() {
        assertEquals("N/A", DiscordEmbed.sanitize(null, 100));
        assertEquals("N/A", DiscordEmbed.sanitize("", 100));
        assertEquals("N/A", DiscordEmbed.sanitize("   ", 100));
    }

    @Test
    public void testFieldLimitMax25() {
        DiscordEmbed embed = new DiscordEmbed();
        for (int i = 0; i < 30; i++) {
            embed.addField("Field " + i, "Value " + i, true);
        }

        assertEquals(25, embed.getFields().size(), "Discord embeds must enforce a maximum of 25 fields");
    }

    @Test
    public void testEmbedJsonSerialization() {
        Instant now = Instant.now();
        DiscordEmbed embed = new DiscordEmbed()
                .setTitle("Test Title")
                .setDescription("Test Description")
                .setColor(0xFF0000)
                .setTimestamp(now)
                .setAuthor("Staff Member", "https://example.com/icon.png")
                .setThumbnail("https://example.com/thumb.png")
                .setFooter("Server 1", "https://example.com/footer.png")
                .addField("Reason", "Cheating", true);

        JsonObject json = embed.toJson();

        assertEquals("Test Title", json.get("title").getAsString());
        assertEquals("Test Description", json.get("description").getAsString());
        assertEquals(0xFF0000, json.get("color").getAsInt());
        assertEquals(now.toString(), json.get("timestamp").getAsString());

        assertTrue(json.has("author"));
        assertEquals("Staff Member", json.getAsJsonObject("author").get("name").getAsString());

        assertTrue(json.has("thumbnail"));
        assertEquals("https://example.com/thumb.png", json.getAsJsonObject("thumbnail").get("url").getAsString());

        assertTrue(json.has("footer"));
        assertEquals("Server 1", json.getAsJsonObject("footer").get("text").getAsString());

        assertTrue(json.has("fields"));
        assertEquals(1, json.getAsJsonArray("fields").size());
        assertEquals("Reason", json.getAsJsonArray("fields").get(0).getAsJsonObject().get("name").getAsString());
        assertEquals("Cheating", json.getAsJsonArray("fields").get(0).getAsJsonObject().get("value").getAsString());
        assertTrue(json.getAsJsonArray("fields").get(0).getAsJsonObject().get("inline").getAsBoolean());
    }
}
