# Heaven Eyes — Advanced Minecraft Anti-Cheat

**Version:** 1.0.0  
**Author / Creator:** LivingYuji  
**Target Server:** Paper 1.21.x (Java 21)  
**Main Command:** `/he` (Aliases: `/ac`, `/anticheat`)  
**Permission Prefix:** `heaveneyes.*`  
**Distribution Artifact:** `HeavenEyes-1.0.0.jar`  

---

## 1. Overview

**Heaven Eyes** is a comprehensive, original, evidence-based anti-cheat plugin designed for high-performance Paper Minecraft servers. Built from the ground up with asynchronous persistence and robust mathematical detection heuristics, Heaven Eyes provides automated enforcement, comprehensive staff investigation tools, player mining statistics, and seamless LuckPerms user management.

---

## 2. Key Features

- **Automated Enforcement System:**
  - 3-warning graduated punishment progression culminating in an automatic 30-minute temporary ban (`WARN_THEN_TEMPBAN`).
  - Cooldown aggregation window (`60s`) preventing warning spam from lag spikes.
  - High-confidence severe violation immediate kick enforcement.
  - Pre-login ban verification (`AsyncPlayerPreLoginEvent`) with stable Ban IDs (`HE-XXXXXX`).

- **Movement Detection Modules:**
  - `SpeedCheck`: Detects horizontal velocity abuse considering sprint-jumps, potion effects, and ice blocks.
  - `FlyCheck`: Detects unsupported vertical ascent, hover states, and air movement.
  - `NoFallCheck`: Identifies client spoofing `onGround=true` packets mid-air.
  - `TimerCheck`: Detects game clock acceleration and excessive movement packet rates.
  - `StepCheck`: Catches instantaneous full-block elevation steps.
  - `VelocityCheck`: Detects anti-knockback and ignored server velocity impulses.

- **Combat Detection Modules:**
  - `ReachCheck`: Calculates eye-to-target bounding box reach with ping latency compensation.
  - `KillAuraCheck`: Detects out-of-FOV hit angles (> 90°) and sub-50ms multi-target switching.
  - `FastClickCheck`: Detects autoclickers and abnormal CPS bursts exceeding human limits.
  - `AimSnapCheck`: Catches instantaneous rotation snapping on attack ticks.
  - `CriticalsCheck`: Detects spoofed critical strikes while grounded.

- **Statistical Mining Analytics & Anti-Xray:**
  - Collects server-side block break tallies (Stone, Deepslate, Diamonds, Ancient Debris, Gold, Iron).
  - Evaluates ore ratios over configurable sample sizes (default 60+ blocks).
  - Defaults to `REVIEW` mode to protect legitimate players from false-positive bans.
  - Integrates with Paper's built-in packet-level Anti-Xray engine.

- **ESP & Freecam Heuristics:**
  - `ContainerESP`: Detects container interactions through solid occluding walls or extreme reach.
  - `Freecam`: Detects remote block breaks detached from player body position.

- **LuckPerms Integration (No Rank Creation):**
  - Assign or revoke individual permission nodes on player accounts directly via the LuckPerms API.
  - Validates permission nodes and protects against administrative privilege escalation.

- **Administrative Tools:**
  - Compressed zip backup generation (`/he backup`).
  - Comprehensive player audit report export (`/he export <player>`).
  - Automated data retention preview and cleanup (`/he cleanup <preview|run>`).
  - Complete tab completion for every command and subcommand.

---

## 3. Installation & Getting Started

1. Place `HeavenEyes-1.0.0.jar` into your Paper server's `plugins/` directory.
2. (Optional) Ensure `LuckPerms` is installed if you wish to use `/he perm` commands.
3. Start the server. Heaven Eyes will generate default configuration files in `plugins/HeavenEyes/`.
4. Review and tune thresholds in `plugins/HeavenEyes/checks.yml` and `punishments.yml`.

---

## 4. Command Quick Reference

| Command | Permission | Description |
|---------|------------|-------------|
| `/he help` | `heaveneyes.help` | View permitted commands |
| `/he version` | `heaveneyes.version` | Display plugin version |
| `/he status` | `heaveneyes.status` | View system status |
| `/he report <player> <reason>` | `heaveneyes.report` | Submit player report to staff |
| `/he inspect <player>` | `heaveneyes.inspect` | Inspect player metadata and violations |
| `/he violations <player>` | `heaveneyes.violations` | View active violation levels |
| `/he history <player>` | `heaveneyes.history` | View warning history and past bans |
| `/he mining <player>` | `heaveneyes.mining` | View ore ratios and mining stats |
| `/he baninfo <id\|player>` | `heaveneyes.baninfo` | View ban details and remaining time |
| `/he kick <player> <reason>` | `heaveneyes.kick` | Kick a player |
| `/he tempban <player> <time> <reason>` | `heaveneyes.ban` | Temporarily ban a player (e.g. `30m`) |
| `/he ban <player> <reason>` | `heaveneyes.ban` | Permanently ban a player |
| `/he unban <player> <reason>` | `heaveneyes.unban` | Unban player by name |
| `/he pardon <ban-id> <reason>` | `heaveneyes.unban` | Pardon ban by Ban ID |
| `/he exempt <player>` | `heaveneyes.exempt` | Exempt player from anti-cheat |
| `/he checks` | `heaveneyes.checks` | List all checks and states |
| `/he check <name> <on\|off>` | `heaveneyes.checks` | Toggle an individual check |
| `/he alerts [on\|off]` | `heaveneyes.alerts` | Toggle staff violation alerts |
| `/he staff <add\|remove\|list>` | `heaveneyes.staff` | Manage staff alert recipients |
| `/he perm <add\|remove\|list\|check>` | `heaveneyes.permissions` | Manage LuckPerms permissions |
| `/he backup` | `heaveneyes.backup` | Create compressed data backup |
| `/he export <player>` | `heaveneyes.export` | Export player audit file |
| `/he cleanup <preview\|run>` | `heaveneyes.cleanup` | Data retention cleanup |
| `/he reload` | `heaveneyes.reload` | Reload configuration files safely |

---

## 5. Discord Webhook Integration (Quick Setup)

Heaven Eyes includes a native, non-blocking Discord webhook notification pipeline with zero server performance impact:

1. Open your Discord server and navigate to your staff category.
2. Create dedicated channels (e.g. `#ac-alerts`, `#ac-punishments`, `#ac-history`, `#ac-system`).
3. In channel settings, select **Integrations** > **Webhooks** > **New Webhook**.
4. Name the webhook (e.g. `Heaven Eyes`) and click **Copy Webhook URL**.
5. Open your Minecraft server directory and navigate to `plugins/HeavenEyes/config.yml`.
6. Set `discord.enabled: true`.
7. Paste your webhook URL into `discord.webhook-urls.alerts` and other channel fields as desired. (You may use the same webhook URL for all channels).
8. Adjust `alerts.minimum-violation-level` (default `3`) to control alert threshold.
9. Tune `alerts.cooldown-seconds` (default `10s`) to prevent alert spam.
10. Verify delivery parameters (`retry-enabled: true`, `queue-capacity: 1000`).
11. Save `config.yml` and execute `/he reload` in-game or from console.
12. Run `/he status` to confirm Discord Webhooks status shows **Enabled**.

For full architectural details, embed schemas, rate-limit policies, and troubleshooting, refer to [Discord Integration Documentation](docs/DISCORD_INTEGRATION.md).

---

## 6. Build Instructions

Requirements: Java 21, Gradle wrapper.

```powershell
.\gradlew.bat clean test build
```

Compiled output: `build/libs/HeavenEyes-1.0.0.jar` (also mirrored in `dist/HeavenEyes-1.0.0.jar`).

---

## 7. Documentation Directory

- [Discord Webhook Integration](docs/DISCORD_INTEGRATION.md)
- [Command Reference](docs/COMMANDS.md)
- [Permissions Reference](docs/PERMISSIONS.md)
- [Configuration Guide](docs/CONFIGURATION.md)
- [Automated Punishment System](docs/PUNISHMENT_SYSTEM.md)
- [Evidence & Privacy Specification](docs/EVIDENCE_AND_PRIVACY.md)
- [Build & Installation Guide](docs/BUILD_AND_INSTALL.md)
- [Paper Anti-Xray Configuration](docs/PAPER_ANTIXRAY.md)

---

## 8. Copyright & License

Copyright (c) 2026 LivingYuji. All rights reserved.  
Original materials licensed under proprietary all-rights-reserved terms. See [COPYRIGHT.md](COPYRIGHT.md) and [LICENSE](LICENSE) for details.

