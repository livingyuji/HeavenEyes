package dev.livingyuji.heaveneyes.discord;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous HTTP client for Discord webhook delivery using standard Java 21 HttpClient.
 */
public class DiscordWebhookClient {

    private final HttpClient httpClient;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(8);

    public DiscordWebhookClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public CompletableFuture<HttpResponse<String>> sendAsync(String webhookUrl, String jsonPayload) {
        if (!DiscordConfig.isValidWebhookUrl(webhookUrl)) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid webhook URL target"));
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/json")
                .header("User-Agent", "HeavenEyes-AntiCheat/1.0.0 (LivingYuji)")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Extracts retry-after delay in milliseconds from Discord response.
     */
    public static long parseRetryAfterMillis(HttpResponse<String> response) {
        if (response == null) return 1000L;

        // Discord provides 'Retry-After' header in seconds (or floating point seconds)
        var header = response.headers().firstValue("Retry-After");
        if (header.isPresent()) {
            try {
                double seconds = Double.parseDouble(header.get());
                return (long) (seconds * 1000.0);
            } catch (NumberFormatException ignored) {}
        }

        // Fallback: check JSON body
        String body = response.body();
        if (body != null && body.contains("retry_after")) {
            try {
                int idx = body.indexOf("retry_after");
                int colon = body.indexOf(':', idx);
                int comma = body.indexOf(',', colon);
                if (comma == -1) comma = body.indexOf('}', colon);
                if (colon != -1 && comma != -1) {
                    String val = body.substring(colon + 1, comma).trim();
                    double seconds = Double.parseDouble(val);
                    return (long) (seconds * 1000.0);
                }
            } catch (Exception ignored) {}
        }

        return 1500L; // Default fallback delay
    }
}
