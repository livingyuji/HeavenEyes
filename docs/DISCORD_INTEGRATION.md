# Heaven Eyes — Discord Webhook Integration Documentation

**Author:** LivingYuji  
**Plugin:** Heaven Eyes Anti-Cheat  
**Target:** Paper 1.21.x (Java 21)  
**Version:** 1.0.0  

---

## 1. Architectural Overview

The Discord integration in Heaven Eyes is designed to bridge in-game anti-cheat alerts, disciplinary actions, player audit history, and system health status into Discord staff channels in real time, without compromising Minecraft server performance.

```
┌────────────────────────────────────────────────────────┐
│               Heaven Eyes Anti-Cheat Core              │
│   (Violation Engine / Punishment Engine / Ban Manager) │
└──────────────────────────┬─────────────────────────────┘
                           │ Dispatches Event (Async)
                           ▼
┌────────────────────────────────────────────────────────┐
│                   DiscordIntegration                   │
│   - Feature toggles (alerts, punishments, logs)        │
│   - Minimum violation level (VL) gating                │
└──────────────────────────┬─────────────────────────────┘
                           │ Generates Embeds
                           ▼
┌────────────────────────────────────────────────────────┐
│                  DiscordMessageBuilder                 │
│   - Sanitizes untrusted strings (mentions / length)    │
│   - Constructs Discord JSON payloads                   │
│   - Sets allowed_mentions: {"parse": []}               │
└──────────────────────────┬─────────────────────────────┘
                           │ Offers to Bounded Queue
                           ▼
┌────────────────────────────────────────────────────────┐
│                  DiscordDeliveryQueue                  │
│   - Bounded LinkedBlockingQueue (default cap: 1000)    │
│   - Single-thread daemon scheduler                     │
│   - Alert suppression cooldowns (prevents flood)       │
│   - Rate limit (HTTP 429) backoff timer                │
│   - Exponential backoff for transient network errors   │
└──────────────────────────┬─────────────────────────────┘
                           │ Sends Asynchronous HTTPS POST
                           ▼
┌────────────────────────────────────────────────────────┐
│                  DiscordWebhookClient                  │
│   - Native Java 21 java.net.http.HttpClient            │
│   - 8-second request timeout, 6-second connect timeout │
└──────────────────────────┬─────────────────────────────┘
                           │
                           ▼
                  Discord API Servers
```

### Key Architectural Guarantees:
1. **Zero Main-Thread Blocking:** Every webhook network request is executed off-thread via Java 21's asynchronous `CompletableFuture` HTTP client. Server TPS is completely unaffected by Discord response latency or temporary network dropouts.
2. **Local YAML Storage is Authoritative:** Persistent player histories (`data/players/`), active bans (`bans/`), and incident records (`evidence/`) are committed locally before Discord dispatches. Discord notifications serve as an audit stream and staff monitoring interface, never as the single source of truth.
3. **No External Libraries Required:** Built using native Java 21 `HttpClient` and shaded `com.google.gson` bundled in Paper, keeping the plugin lightweight (~176 KB). No bulky third-party Discord libraries or bot tokens required.

---

## 2. Webhook Security & Mention Protection

### A. Secret Masking
Webhook URLs contain sensitive authentication tokens. Heaven Eyes enforces strict security controls:
- Webhook tokens are **never** logged to the console, debug logs, or exported incident reports.
- Methods such as `DiscordConfig.maskWebhookUrl(...)` mask tokens to `https://discord.com/api/webhooks/<id>/***`.
- Webhook target URLs are validated using strict HTTPS regular expressions (`^https://(?:ptb\.|canary\.)?(?:discord\.com|discordapp\.com)/api/webhooks/\d+/[A-Za-z0-9_-]+$`). Insecure `http://` or non-Discord endpoints are rejected immediately.

### B. Mention Injection Prevention
Malicious players often attempt to manipulate anti-cheat alerts by spoofing names or chat messages containing `@everyone`, `@here`, or `<@&role_id>`. Heaven Eyes neutralizes this at two distinct layers:
1. **API-Level Mention Disabling:** Every outgoing payload explicitly includes:
   ```json
   "allowed_mentions": {
     "parse": []
   }
   ```
   This prevents Discord from triggering user, role, or broadcast pings regardless of text content.
2. **String Sanitization:** All user-controlled fields (player names, reasons, violation explanations) are passed through `DiscordEmbed.sanitize(...)`:
   - `@everyone` is replaced with `@\u200Beveryone` (zero-width space).
   - `@here` is replaced with `@\u200Bhere`.
   - `<@` is replaced with `<@\u200B`.
   - Character counts are clamped to Discord embed limits (title <= 256, description <= 4096, field name <= 256, field value <= 1024, max 25 fields).

---

## 3. Delivery Queue, Rate Limiting & Reliability

Discord enforces strict rate limits on webhook endpoints (standard: 5 requests per 2 seconds per webhook). Heaven Eyes features an adaptive delivery pipeline to maintain seamless throughput:

### A. HTTP 429 Handling
When Discord returns `HTTP 429 Too Many Requests`:
1. `DiscordWebhookClient.parseRetryAfterMillis(...)` extracts the required wait time from the `Retry-After` HTTP header or JSON body.
2. The `DiscordDeliveryQueue` scheduler pauses queue processing for the exact duration plus a 100ms safety buffer.
3. The affected payload is re-offered to the front of the queue without consuming retry counts.

### B. Bounded Memory Safety
- In situations where Discord is unreachable or network connectivity is severed, outgoing messages enter a bounded `LinkedBlockingQueue` (capacity configurable via `delivery.queue-capacity`, default: 1000).
- If the queue reaches capacity, new events are dropped with a warning log rather than consuming server heap memory.

### C. Exponential Backoff Retries
- Transient errors (HTTP 5xx server errors, network connection resets, DNS timeouts) are retried with exponential backoff: `2^attempt * 1000ms` (up to a 30-second cap).
- Permanent errors (HTTP 400 Bad Request, HTTP 404 Webhook Deleted) are dropped immediately without wasteful retries.

### D. Duplicate Alert Suppression
- Violations can trigger rapidly during continuous player movement or combat engagements.
- `DiscordDeliveryQueue.shouldSuppressAlert(key, cooldownSeconds)` maintains a per-player per-check cooldown (default: 10 seconds).
- Repeated flags for the same player within the cooldown window update the local violation level but do not flood Discord channels.

---

## 4. Rich Embed Specifications & Schemas

### A. Live Anti-Cheat Alerts
- **Color:** `#E74C3C` (Crimson Red)
- **Thumbnail:** Player head avatar (`https://mc-heads.net/avatar/<uuid>/100`)
- **Fields:**
  - `Player`: Username and UUID
  - `Check`: Check name and category (e.g. `Speed (Movement)`)
  - `Severity`: Active Violation Level (VL) and Confidence percentage
  - `Server`: Configured server name
  - `Action / Status`: Enforcement action taken (e.g. `Cancelled Movement`, `Flagged & Monitored`)
  - `Evidence ID`: Associated incident ID (`INC-XXXXXX`)
  - `Detection Summary`: Mathematical explanation of why the threshold was crossed
  - `Diagnostic Metrics`: JSON-formatted bounding box / delta telemetry

### B. Punishment Logs
- **Warnings (`#F1C40F` Gold):** Displays warning count against max warnings (e.g. `2/3`), check type, reason, evidence ID.
- **Kicks (`#E67E22` Orange):** Displays player, enforcing actor, reason, evidence ID.
- **Temporary Bans (`#992D22` Dark Red):** Displays Ban ID, duration (e.g. `30m`), expiration timestamp, reason, actor, evidence ID.
- **Permanent Bans (`#992D22` Dark Red):** Displays Ban ID, permanent duration flag, reason, actor, evidence ID.
- **Unbans / Pardons (`#2ECC71` Emerald Green):** Displays player, Ban ID, pardoning staff member, original reason, pardon reason.

### C. Player History Audit
- **Color:** `#3498DB` (Blue)
- **Fields:** Player, Event Type (`Flag History`, `Warning History`), summary of violations or behavioral shifts.

### D. System Lifecycle Logs
- **Color:** `#1ABC9C` (Teal) for operational notices; `#C0392B` (Dark Red) for critical errors.
- **Fields:** Lifecycle events (Startup, Shutdown, Reload, Repeated delivery failures).

---

## 5. Configuration Reference

All Discord settings are managed under the `discord:` section in `plugins/HeavenEyes/config.yml`:

```yaml
discord:
  # Enable or disable the entire Discord webhook integration
  enabled: false

  # Friendly name displayed in embed footers and headers
  server-name: "Heaven Eyes Survival"

  # Webhook endpoints (leave blank to disable specific channels, or use one for all)
  webhook-urls:
    alerts: ""        # Dedicated channel for live violation alerts
    punishments: ""   # Dedicated channel for warnings, kicks, bans, unbans
    history: ""       # Channel for player history audit entries
    system-logs: ""   # Channel for startup/shutdown and error notices

  # Violation alert settings
  alerts:
    enabled: true
    # Minimum violation level (VL) before sending to Discord
    minimum-violation-level: 3
    # Seconds to suppress repeated alerts for the same player & check
    cooldown-seconds: 10
    # Group repeated flags during the cooldown window
    batch-repeated-alerts: true

  # Punishment logging settings
  punishments:
    enabled: true
    log-warnings: true
    log-kicks: true
    log-temporary-bans: true
    log-permanent-bans: true
    log-unbans: true

  # Player history audit logging settings
  player-history:
    enabled: true

  # System operational logs (startup, shutdown, critical issues)
  system-logs:
    enabled: true

  # Delivery engine tuning
  delivery:
    asynchronous: true
    retry-enabled: true
    max-retries: 5
    queue-capacity: 1000
```

---

## 6. Recommended Discord Channel Setup

For optimal staff workflow and moderation clarity, set up dedicated channels:

| Channel Name | Recommended Webhook Key | Purpose |
|--------------|-------------------------|---------|
| `#ac-alerts` | `webhook-urls.alerts` | High-frequency notifications of active player cheat violations |
| `#ac-punishments` | `webhook-urls.punishments` | Immutable log of staff and automated warnings, kicks, and bans |
| `#ac-history` | `webhook-urls.history` | Player audit trails, evidence registrations, and profile snapshots |
| `#ac-system` | `webhook-urls.system-logs` | Server startup/shutdown notices and webhook health diagnostics |

> [!TIP]
> If your server has a single staff-logs channel, you can paste the **same webhook URL** into all four configuration fields. Heaven Eyes will route all embeds to that single channel seamlessly.

---

## 7. Troubleshooting & FAQ

### Alerts are not appearing in Discord
1. Check that `discord.enabled: true` in `config.yml`.
2. Verify that the webhook URL begins with `https://discord.com/api/webhooks/` or `https://discordapp.com/api/webhooks/`.
3. Check `alerts.minimum-violation-level`. Minor flags below this threshold will not be dispatched.
4. Run `/he status` in-game to check the live Discord status.

### HTTP 429 Rate Limit warnings in console
- Occasional rate-limiting notices are normal during high server activity. Heaven Eyes automatically pauses the queue and retries when Discord's `Retry-After` window expires.
- If warnings are frequent, increase `alerts.cooldown-seconds` (e.g. from `10` to `20`) or increase `alerts.minimum-violation-level`.

### Webhook returns HTTP 404
- The webhook was deleted in Discord. Create a new webhook in Discord Channel Settings and update `config.yml`, then run `/he reload`.
