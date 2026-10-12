# COMMAND REFERENCE — HEAVEN EYES

**Primary Command:** `/he`  
**Aliases:** `/ac`, `/anticheat`  
**Author:** LivingYuji  
**Platform:** Paper 1.21.x  

---

## 1. Public & General Commands

### `/he` or `/he help`
- **Description:** Displays the interactive help page showing commands the sender is permitted to execute.
- **Permission:** `heaveneyes.help` (Default: `true` for all players)
- **Example:** `/he help`

### `/he version`
- **Description:** Displays the running version of the Heaven Eyes plugin and creator attribution.
- **Permission:** `heaveneyes.version` (Default: `true`)
- **Example:** `/he version`
- **Response:** `Running Heaven Eyes version 1.0.0 by LivingYuji.`

### `/he status`
- **Description:** Displays safe, public-friendly anti-cheat system status, uptime, active checks, and LuckPerms integration state.
- **Permission:** `heaveneyes.status` (Default: `true`)
- **Example:** `/he status`

### `/he report <player> <reason>`
- **Description:** Submits a suspicious player report directly to online staff members.
- **Permission:** `heaveneyes.report` (Default: `true`)
- **Example:** `/he report Player123 Suspicious reach during PvP in arena`
- **Response:** `Thank you. Your report against Player123 for '...' has been submitted to staff.`

---

## 2. Staff Inspection & Violation Analysis

### `/he inspect <player>`
- **Description:** Inspects a player's real-time runtime status, ping, exemption status, total violation history, and warning counts.
- **Permission:** `heaveneyes.inspect` (Default: `op`)
- **Example:** `/he inspect Alex`

### `/he violations <player>`
- **Description:** Displays active violation levels (VL) across all enabled detection modules for the specified player.
- **Permission:** `heaveneyes.violations` (Default: `op`)
- **Example:** `/he violations Alex`

### `/he evidence <player>`
- **Description:** Lists all persistent incident evidence files and references associated with the player.
- **Permission:** `heaveneyes.evidence` (Default: `op`)
- **Example:** `/he evidence Alex`

### `/he history <player>` or `/he punishments <player>`
- **Description:** Displays complete historical warning records and past/active bans for a player.
- **Permission:** `heaveneyes.history` (Default: `op`)
- **Example:** `/he history Alex`

### `/he mining <player>`
- **Description:** Generates an in-depth mining analytics report showing base blocks mined, diamond and ancient debris ratios, and suspicious vein indicators.
- **Permission:** `heaveneyes.mining` (Default: `op`)
- **Example:** `/he mining Steve`

### `/he baninfo <player|ban-id>`
- **Description:** Retrieves detailed metadata for an active or historical ban, including ban ID, source, actor, reason, and remaining duration.
- **Permission:** `heaveneyes.baninfo` (Default: `op`)
- **Example:** `/he baninfo HE-A8F2K9`

---

## 3. Punishment & Enforcement Commands

### `/he kick <player> <reason>`
- **Description:** Manually kicks a player from the server with anti-cheat branding and logs the action to `audit/audit.log`.
- **Permission:** `heaveneyes.kick` (Default: `op`)
- **Example:** `/he kick BadActor Unacceptable combat anomaly`

### `/he ban <player> <reason>`
- **Description:** Issues a permanent ban, persists it immediately to `bans/active.yml`, and kicks the player if currently online.
- **Permission:** `heaveneyes.ban` (Default: `op`)
- **Example:** `/he ban BadActor Confirmed severe KillAura usage`

### `/he tempban <player> <duration> <reason>`
- **Description:** Issues a temporary ban with validated duration syntax (e.g. `30m`, `1h`, `24h`, `7d`).
- **Permission:** `heaveneyes.ban` (Default: `op`)
- **Example:** `/he tempban BadActor 30m Speed violation threshold reached`

### `/he unban <player> <reason>`
- **Description:** Unbans a player by player name, pardons the active ban, retains the historical record in `bans/history.yml`, and audits the staff actor.
- **Permission:** `heaveneyes.unban` (Default: `op`)
- **Example:** `/he unban BadActor Ban appeal approved by administration`

### `/he pardon <ban-id> <reason>`
- **Description:** Pardons an active ban using its unique stable Ban ID (`HE-XXXXXX`).
- **Permission:** `heaveneyes.unban` (Default: `op`)
- **Example:** `/he pardon HE-A8F2K9 False positive during server lag spike`

### `/he exempt <player>`
- **Description:** Adds a player to the runtime exemption list, bypassing all anti-cheat checks.
- **Permission:** `heaveneyes.exempt` (Default: `op`)
- **Example:** `/he exempt Developer1`

### `/he unexempt <player>`
- **Description:** Removes a player from the exemption list.
- **Permission:** `heaveneyes.exempt` (Default: `op`)
- **Example:** `/he unexempt Developer1`

---

## 4. Staff Management

### `/he staff add <player>`
- **Description:** Adds a staff member to `staff.yml` and enrolls them into real-time violation alerts.
- **Permission:** `heaveneyes.staff` (Default: `op`)
- **Example:** `/he staff add ModeratorSam`

### `/he staff remove <player>`
- **Description:** Removes a staff member from `staff.yml`.
- **Permission:** `heaveneyes.staff` (Default: `op`)
- **Example:** `/he staff remove ModeratorSam`

### `/he staff list`
- **Description:** Lists all registered staff members and their current alert subscription status.
- **Permission:** `heaveneyes.staff` (Default: `op`)
- **Example:** `/he staff list`

### `/he alerts [on|off]`
- **Description:** Toggles real-time staff violation alerts for the command sender.
- **Permission:** `heaveneyes.alerts` (Default: `op`)
- **Example:** `/he alerts off`

---

## 5. LuckPerms Direct Permission Management

*Note: These commands modify individual player user data through the LuckPerms API without creating ranks or groups.*

### `/he perm add <player> <permission>`
- **Description:** Grants a validated permission node directly to a player's LuckPerms user data.
- **Permission:** `heaveneyes.permissions` (Default: `op`)
- **Example:** `/he perm add Alex heaveneyes.alerts`

### `/he perm remove <player> <permission>`
- **Description:** Revokes a permission node directly from a player's user data.
- **Permission:** `heaveneyes.permissions` (Default: `op`)
- **Example:** `/he perm remove Alex heaveneyes.alerts`

### `/he perm list <player>`
- **Description:** Lists all direct user permissions assigned to the player.
- **Permission:** `heaveneyes.permissions` (Default: `op`)
- **Example:** `/he perm list Alex`

### `/he perm check <player> <permission>`
- **Description:** Checks whether a player possesses a specific permission node.
- **Permission:** `heaveneyes.permissions` (Default: `op`)
- **Example:** `/he perm check Alex heaveneyes.inspect`

---

## 6. Data, Diagnostics & Maintenance

### `/he checks`
- **Description:** Lists all registered detection modules, categories, statuses, and configured actions.
- **Permission:** `heaveneyes.checks` (Default: `op`)
- **Example:** `/he checks`

### `/he check <check> <on|off>`
- **Description:** Toggles an individual detection check on or off at runtime.
- **Permission:** `heaveneyes.checks` (Default: `op`)
- **Example:** `/he check Speed off`

### `/he backup`
- **Description:** Generates an asynchronous compressed ZIP backup in `plugins/HeavenEyes/backups/`.
- **Permission:** `heaveneyes.backup` (Default: `op`)
- **Example:** `/he backup`

### `/he export <player>`
- **Description:** Generates a human-readable comprehensive audit report in `plugins/HeavenEyes/exports/`.
- **Permission:** `heaveneyes.export` (Default: `op`)
- **Example:** `/he export Alex`

### `/he cleanup preview`
- **Description:** Previews how many incident files and old records exceed the configured retention period.
- **Permission:** `heaveneyes.cleanup` (Default: `op`)
- **Example:** `/he cleanup preview`

### `/he cleanup run`
- **Description:** Executes data retention cleanup, purging expired incident files and recording to audit log.
- **Permission:** `heaveneyes.cleanup` (Default: `op`)
- **Example:** `/he cleanup run`

### `/he reload`
- **Description:** Safely reloads all YAML configuration files, check thresholds, and ban records without restarting the server or duplicating listeners.
- **Permission:** `heaveneyes.reload` (Default: `op`)
- **Example:** `/he reload`
