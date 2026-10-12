package dev.livingyuji.heaveneyes.discord;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DiscordConfigTest {

    @Test
    public void testValidWebhookUrls() {
        assertTrue(DiscordConfig.isValidWebhookUrl("https://discord.com/api/webhooks/123456789/abcdef-12345_xyz"));
        assertTrue(DiscordConfig.isValidWebhookUrl("https://discordapp.com/api/webhooks/9876543210/TOKEN_ABC-123"));
        assertTrue(DiscordConfig.isValidWebhookUrl("https://ptb.discord.com/api/webhooks/1122334455/my-sample-token"));
        assertTrue(DiscordConfig.isValidWebhookUrl("https://canary.discord.com/api/webhooks/1122334455/my-sample-token"));
    }

    @Test
    public void testInvalidWebhookUrls() {
        assertFalse(DiscordConfig.isValidWebhookUrl(null));
        assertFalse(DiscordConfig.isValidWebhookUrl(""));
        assertFalse(DiscordConfig.isValidWebhookUrl("   "));
        assertFalse(DiscordConfig.isValidWebhookUrl("http://discord.com/api/webhooks/12345/token")); // Non-HTTPS
        assertFalse(DiscordConfig.isValidWebhookUrl("https://evil.com/api/webhooks/12345/token"));
        assertFalse(DiscordConfig.isValidWebhookUrl("https://discord.com/api/webhooks/not-numeric/token"));
        assertFalse(DiscordConfig.isValidWebhookUrl("https://discord.com/api/webhooks/12345")); // Missing token
    }

    @Test
    public void testMaskWebhookUrl() {
        String webhook = "https://discord.com/api/webhooks/123456789/SuperSecretToken123";
        String masked = DiscordConfig.maskWebhookUrl(webhook);

        assertFalse(masked.contains("SuperSecretToken123"), "Masked URL must not contain the secret token");
        assertTrue(masked.contains("123456789"), "Masked URL should retain the channel ID prefix");
        assertTrue(masked.endsWith("/***"));

        assertEquals("<none>", DiscordConfig.maskWebhookUrl(null));
        assertEquals("<none>", DiscordConfig.maskWebhookUrl(""));
    }

    @Test
    public void testWebhookFallback() {
        DiscordConfig config = new DiscordConfig();
        // By default, no webhooks configured
        assertNull(config.resolveAlertsWebhook());
        assertNull(config.resolvePunishmentsWebhook());
        assertNull(config.resolveHistoryWebhook());
        assertNull(config.resolveSystemLogsWebhook());
    }
}
