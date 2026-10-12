package dev.livingyuji.heaveneyes.discord;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class DiscordDeliveryQueueTest {

    private DiscordWebhookClient client;
    private DiscordConfig config;
    private Logger logger;
    private DiscordDeliveryQueue queue;

    @BeforeEach
    public void setUp() {
        client = new DiscordWebhookClient();
        config = new DiscordConfig();
        logger = Logger.getLogger("DiscordDeliveryQueueTest");
        queue = new DiscordDeliveryQueue(client, config, logger);
    }

    @AfterEach
    public void tearDown() {
        if (queue != null) {
            queue.shutdown(1000);
        }
    }

    @Test
    public void testAlertSuppressionCooldown() {
        String alertKey = "player-uuid-1:Speed";

        // First alert should not be suppressed
        assertFalse(queue.shouldSuppressAlert(alertKey, 5), "Initial alert should not be suppressed");

        // Immediate subsequent alert should be suppressed by the 5-second cooldown
        assertTrue(queue.shouldSuppressAlert(alertKey, 5), "Repeated alert within cooldown window should be suppressed");

        // A different alert key should not be suppressed
        assertFalse(queue.shouldSuppressAlert("player-uuid-2:Fly", 5), "Different player/check key should not be suppressed");

        // When cooldown is 0, suppression is disabled
        assertFalse(queue.shouldSuppressAlert("player-uuid-1:Speed", 0), "Cooldown <= 0 should never suppress");
    }

    @Test
    public void testEnqueueRejectsInvalidOrNull() {
        assertFalse(queue.enqueue(null, "{}"), "Null webhook URL should be rejected");
        assertFalse(queue.enqueue("https://discord.com/api/webhooks/123/token", null), "Null payload should be rejected");
    }

    @Test
    public void testEnqueueAndQueueSize() {
        String testUrl = "https://discord.com/api/webhooks/123456789/valid-token-xyz";
        String payload = "{\"content\":\"test\"}";

        int initialSize = queue.getQueueSize();
        assertEquals(0, initialSize);

        boolean enqueued = queue.enqueue(testUrl, payload);
        assertTrue(enqueued, "Valid message should be accepted into queue");
    }

    @Test
    public void testShutdownStopsProcessing() {
        queue.shutdown(500);

        // After shutdown, enqueue should return false
        boolean offered = queue.enqueue("https://discord.com/api/webhooks/123/token", "{}");
        assertFalse(offered, "Enqueue after shutdown must be rejected");
    }
}
