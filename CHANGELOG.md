# CHANGELOG — HEAVEN EYES

All notable changes to the Heaven Eyes anti-cheat project will be documented in this file.

---

## [1.0.0] - 2026-10-12
### Initial Production Release — Author: LivingYuji

#### Added
- **Core Engine:**
  - Complete plugin architecture targeting Paper 1.21.x and Java 21.
  - Asynchronous persistent storage architecture for player profiles, violations, warnings, and bans.
  - Multi-file configuration system (`config.yml`, `checks.yml`, `punishments.yml`, `messages.yml`, `staff.yml`).
  - Safe configuration reload command (`/he reload`).

- **Automated Punishment Engine:**
  - 3-warning progression model leading to an automatic 30-minute temporary ban (`WARN_THEN_TEMPBAN`).
  - Configurable repeated-detection rate-limiting window (default `60s`) preventing false-positive spikes.
  - High-confidence severe violation immediate kick enforcement.
  - Unique Ban ID generation (`HE-XXXXXX`) and join-time pre-login ban enforcement.
  - Automatic expiration and pardon tracking with full audit logging.

- **Detection Modules:**
  - **Movement:** `SpeedCheck`, `FlyCheck`, `NoFallCheck`, `TimerCheck`, `StepCheck`, and `VelocityCheck`.
  - **Combat:** `ReachCheck` (with ping compensation), `KillAuraCheck` (angle & rapid switching), `FastClickCheck` (CPS & burst), `AimSnapCheck`, and `CriticalsCheck`.
  - **Mining Analytics:** Statistical ore-to-stone ratio tracking (`Diamond`, `Ancient Debris`, `Gold`, `Iron`), sampling windows, and trajectory analysis (`REVIEW` mode).
  - **ESP & Freecam:** Container occlusion checks (`ContainerESP`) and remote interaction detection (`Freecam`) in review mode.

- **Staff Experience & Administration:**
  - Public discoverability for root command `/he`, `/he help`, `/he version`, `/he status`, and `/he report`.
  - Permission-protected staff commands for inspection, history, evidence, bans, kicks, pardons, and exemptions.
  - Comprehensive tab completion for every command and subcommand.
  - Real-time staff violation alerts with configurable per-player cooldowns.

- **Integrations & Data Management:**
  - Direct LuckPerms API permission assignment and revocation on user data without creating ranks.
  - Compressed zip backup generation (`/he backup`).
  - Human-readable player audit file export (`/he export <player>`).
  - Automated data retention preview and cleanup (`/he cleanup <preview|run>`).
