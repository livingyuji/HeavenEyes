package dev.livingyuji.heaveneyes.discord;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Manages asynchronous bounded queueing, rate limiting, and exponential backoff retry.
 */
public class DiscordDeliveryQueue {

    private final DiscordWebhookClient client;
    private final DiscordConfig config;
    private final Logger logger;

    private final BlockingQueue<QueuedMessage> queue;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);

    private final Map<String, Long> alertCooldowns = new ConcurrentHashMap<>();
    private volatile long pausedUntilTimestamp = 0;

    public DiscordDeliveryQueue(DiscordWebhookClient client, DiscordConfig config, Logger logger) {
        this.client = client;
        this.config = config;
        this.logger = logger;
        this.queue = new LinkedBlockingQueue<>(config.getDeliveryQueueCapacity());
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "HeavenEyes-DiscordQueue");
            t.setDaemon(true);
            return t;
        });

        // Start periodic drain task
        scheduler.scheduleWithFixedDelay(this::processNext, 100, 100, TimeUnit.MILLISECONDS);
    }

    public boolean shouldSuppressAlert(String alertKey, int cooldownSeconds) {
        if (cooldownSeconds <= 0) return false;
        long now = System.currentTimeMillis();
        Long lastAlert = alertCooldowns.get(alertKey);
        if (lastAlert != null && (now - lastAlert) < (cooldownSeconds * 1000L)) {
            return true;
        }
        alertCooldowns.put(alertKey, now);
        return false;
    }

    public boolean enqueue(String webhookUrl, String payload) {
        if (!running.get() || webhookUrl == null || payload == null) {
            return false;
        }

        QueuedMessage msg = new QueuedMessage(webhookUrl, payload, 0);
        boolean offered = queue.offer(msg);
        if (!offered) {
            logger.warning("[Discord] Delivery queue full (capacity: " + config.getDeliveryQueueCapacity() + "). Dropping message to protect server memory.");
        }
        return offered;
    }

    private void processNext() {
        if (!running.get() || !isProcessing.compareAndSet(false, true)) {
            return;
        }

        try {
            long now = System.currentTimeMillis();
            if (now < pausedUntilTimestamp) {
                return; // Respecting Discord rate-limit pause
            }

            QueuedMessage msg = queue.poll();
            if (msg == null) {
                return;
            }

            dispatchMessage(msg);
        } catch (Exception e) {
            logger.warning("[Discord] Unexpected error processing delivery queue: " + e.getMessage());
        } finally {
            isProcessing.set(false);
        }
    }

    private void dispatchMessage(QueuedMessage msg) {
        client.sendAsync(msg.webhookUrl, msg.payload).thenAccept(response -> {
            int status = response.statusCode();

            if (status >= 200 && status < 300) {
                // Success (200 OK / 204 No Content)
                return;
            }

            if (status == 429) {
                // Rate limited by Discord
                long retryAfter = DiscordWebhookClient.parseRetryAfterMillis(response);
                pausedUntilTimestamp = System.currentTimeMillis() + retryAfter + 100L;
                logger.info("[Discord] Rate limit encountered. Pausing delivery for " + retryAfter + "ms.");

                // Re-enqueue without consuming retry attempts
                if (!queue.offer(msg)) {
                    logger.warning("[Discord] Queue full while requeuing rate-limited payload.");
                }
                return;
            }

            if (status >= 400 && status < 500) {
                // Permanent client error (400 Bad Request, 404 Not Found, etc.)
                logger.warning("[Discord] Permanent delivery error (HTTP " + status + "). Dropping message. Body: " + response.body());
                return;
            }

            // Server error (HTTP 5xx) - transient failure
            handleTransientFailure(msg, "HTTP " + status);
        }).exceptionally(ex -> {
            // Network I/O or connection error - transient failure
            handleTransientFailure(msg, ex.getMessage());
            return null;
        });
    }

    private void handleTransientFailure(QueuedMessage msg, String errorReason) {
        if (!config.isDeliveryRetryEnabled() || msg.retryCount >= config.getDeliveryMaxRetries()) {
            logger.warning("[Discord] Webhook delivery failed after " + msg.retryCount + " attempts: " + errorReason + ". Dropping.");
            return;
        }

        int nextRetry = msg.retryCount + 1;
        long backoffDelayMs = (long) Math.min(30000, Math.pow(2, nextRetry) * 1000L);

        scheduler.schedule(() -> {
            if (running.get()) {
                QueuedMessage retryMsg = new QueuedMessage(msg.webhookUrl, msg.payload, nextRetry);
                queue.offer(retryMsg);
            }
        }, backoffDelayMs, TimeUnit.MILLISECONDS);
    }

    public void shutdown(long timeoutMillis) {
        running.set(false);
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public int getQueueSize() {
        return queue.size();
    }

    private static class QueuedMessage {
        final String webhookUrl;
        final String payload;
        final int retryCount;

        QueuedMessage(String webhookUrl, String payload, int retryCount) {
            this.webhookUrl = webhookUrl;
            this.payload = payload;
            this.retryCount = retryCount;
        }
    }
}
