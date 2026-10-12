# CONFIGURATION GUIDE — HEAVEN EYES

Heaven Eyes isolates configuration across focused YAML files inside the `plugins/HeavenEyes/` directory:

```text
plugins/HeavenEyes/
├── config.yml        # General settings, alerts, retention, exemptions
├── checks.yml        # Individual check toggles, thresholds, and actions
├── punishments.yml   # Warning progression, ban duration, and templates
├── messages.yml      # Colorized chat messages and staff alert formatting
└── staff.yml         # Registered staff members and alert preferences
```

---

## 1. `config.yml` — Core Settings

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `settings.debug` | `false` | Boolean | Outputs detailed internal flag metrics to server console. |
| `settings.alerts-enabled` | `true` | Boolean | Master switch enabling or disabling in-game staff alerts. |
| `settings.alert-cooldown` | `"3s"` | Duration | Minimum cooldown period between alerts for the same player and check. |
| `settings.public-status` | `true` | Boolean | Allows players without staff permissions to view safe status in `/he status`. |
| `settings.tick-interval` | `20` | Integer | Server tick interval for background violation decay task (20 ticks = 1s). |
| `settings.retention-days`| `30` | Integer | Default days of retention before old incidents and data are eligible for cleanup. |
| `settings.evidence.max-in-memory-incidents` | `500` | Integer | Bounded in-memory incident cache size to prevent memory leaks. |
| `exemptions.exempt-creative` | `true` | Boolean | Automatically exempts players in Creative mode from anti-cheat checks. |
| `exemptions.exempt-spectator` | `true` | Boolean | Automatically exempts players in Spectator mode from anti-cheat checks. |

---

## 2. `checks.yml` — Detection Modules

Heaven Eyes categorizes checks into five distinct families:

### Movement (`checks.movement`)
- `minimum-confidence`: Minimum statistical confidence required to trigger action (default `0.90`).
- `automatic-action`: Action policy applied (`WARN_THEN_TEMPBAN`).
- `speed.max-horizontal-speed`: Base horizontal speed limit (default `0.36`).
- `speed.sprint-jump-multiplier`: Sprint-jump physics modifier (default `1.45`).
- `flight.max-air-ticks`: Maximum ticks permitted airborne without ground contact (default `30`).
- `nofall.min-fall-distance`: Minimum fall height before ground-spoof checking (default `3.5`).
- `timer.max-packets-per-second`: Maximum movement updates permitted per second (default `24`).
- `step.max-step-height`: Maximum instant elevation step height (default `0.60`).
- `velocity.knockback-grace-ticks`: Grace window for knockback response (default `25`).

### Combat (`checks.combat`)
- `minimum-confidence`: Default `0.95`.
- `automatic-action`: Default `WARN_THEN_TEMPBAN`.
- `reach.max-reach-distance`: Maximum allowed eye-to-target reach in blocks (default `3.80`).
- `killaura.max-hit-angle`: Maximum line-of-sight attack angle in degrees (default `90.0`).
- `fastclick.max-cps`: Maximum sustained clicks per second (default `20`).
- `aimsnap.max-angle-snap`: Maximum instantaneous yaw rotation snap (default `85.0`).

### Mining Analytics (`checks.mining`)
- `automatic-action`: **`REVIEW`** (Never automatically bans).
- `min-sample-blocks`: Minimum base stone/deepslate required before ratio evaluation (default `60`).
- `diamond-ratio-threshold`: Ore ratio flagging threshold (default `0.18` or 18%).
- `debris-ratio-threshold`: Ancient debris ratio flagging threshold (default `0.12` or 12%).

### ESP & Freecam (`checks.esp`, `checks.freecam`)
- `automatic-action`: **`REVIEW`** (Evidence collection only).
- `esp.wall-occlusion-check`: Checks container interactions through solid walls.
- `freecam.max-interaction-distance`: Maximum reach from physical body (default `6.0`).

---

## 3. `punishments.yml` — Automation Policy

| Key | Default | Description |
|-----|---------|-------------|
| `punishments.automatic` | `true` | Enables or disables automated enforcement globally. |
| `punishments.warnings-before-tempban` | `3` | Number of confirmed warnings required before applying temporary ban. |
| `punishments.tempban-duration` | `"30m"` | Duration applied on automatic ban (e.g. `30m`, `1h`, `1d`). |
| `punishments.warning-reset-after` | `"24h"` | Period after which unused player warnings expire. |
| `punishments.repeated-detection-window` | `"60s"` | Aggregation cooldown window preventing warning spam. |
| `punishments.severe-kick-enabled` | `true` | Immediately kicks players on extreme confidence violations (>= 0.98). |
| `punishments.prevent-duplicate-punishments` | `true` | Concurrency lock preventing double-punishments in the same tick. |

---

## 4. `messages.yml` & `staff.yml`

- `messages.yml` contains fully customizable, color-coded strings supporting `{player}`, `{check}`, `{count}`, `{maximum}`, `{duration}`, `{reason}`, and `{ban_id}` placeholders.
- `staff.yml` records registered staff UUIDs and their personal alert preferences.
