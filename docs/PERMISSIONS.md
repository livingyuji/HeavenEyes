# PERMISSIONS REFERENCE — HEAVEN EYES

**Permission Namespace:** `heaveneyes.*`  
**Platform:** Paper 1.21.x  

All Heaven Eyes administrative and staff commands are strictly enforced on the server thread. Tab completion filters out unauthorized commands so normal players cannot discover protected administrative paths.

---

## 1. Master Permission Nodes

| Node | Description | Default |
|------|-------------|---------|
| `heaveneyes.*` | Grants full administrative access to all Heaven Eyes features, checks, and commands. | `op` |
| `heaveneyes.admin` | Administrative parent node granting all staff, moderation, and data management permissions. | `op` |

---

## 2. Public Player Permissions

These permissions are granted to all players by default to enable client command discovery, help menus, reporting, and version verification without giving access to staff controls:

| Node | Description | Default |
|------|-------------|---------|
| `heaveneyes.help` | Allows running `/he` and `/he help` to see commands permitted for the sender. | `true` |
| `heaveneyes.version` | Allows running `/he version` to check the running plugin build. | `true` |
| `heaveneyes.status` | Allows viewing safe public anti-cheat status (`/he status`). | `true` |
| `heaveneyes.report` | Allows submitting player reports to staff via `/he report <player> <reason>`. | `true` |

---

## 3. Staff & Moderation Permissions

| Node | Description | Default |
|------|-------------|---------|
| `heaveneyes.alerts` | Allows receiving real-time in-game staff violation alerts and toggling alert visibility (`/he alerts`). | `op` |
| `heaveneyes.inspect` | Allows inspecting real-time player metadata, ping, and profile statistics (`/he inspect`). | `op` |
| `heaveneyes.violations` | Allows viewing current violation levels per check for any player (`/he violations`). | `op` |
| `heaveneyes.evidence` | Allows viewing recorded incident evidence files and identifiers (`/he evidence`). | `op` |
| `heaveneyes.history` | Allows viewing comprehensive warning history and ban logs (`/he history`, `/he punishments`). | `op` |
| `heaveneyes.mining` | Allows viewing detailed mining analytics and ore discovery ratios (`/he mining`). | `op` |
| `heaveneyes.baninfo` | Allows viewing detailed ban information by player name or Ban ID (`/he baninfo`). | `op` |
| `heaveneyes.kick` | Allows manually kicking players with anti-cheat reasons (`/he kick`). | `op` |
| `heaveneyes.ban` | Allows issuing permanent and temporary bans (`/he ban`, `/he tempban`). | `op` |
| `heaveneyes.unban` | Allows unbanning players or pardoning specific ban IDs (`/he unban`, `/he pardon`). | `op` |
| `heaveneyes.exempt` | Allows managing player exemptions from anti-cheat checks (`/he exempt`, `/he unexempt`). | `op` |

---

## 4. Administration & Maintenance Permissions

| Node | Description | Default |
|------|-------------|---------|
| `heaveneyes.checks` | Allows listing all checks and toggling checks on or off at runtime (`/he checks`, `/he check`). | `op` |
| `heaveneyes.staff` | Allows adding or removing staff members from `staff.yml` (`/he staff`). | `op` |
| `heaveneyes.permissions`| Allows direct LuckPerms permission management on player accounts (`/he perm`). | `op` |
| `heaveneyes.backup` | Allows generating full plugin ZIP backups (`/he backup`). | `op` |
| `heaveneyes.export` | Allows exporting human-readable player audit files (`/he export`). | `op` |
| `heaveneyes.cleanup` | Allows previewing and running data retention cleanups (`/he cleanup`). | `op` |
| `heaveneyes.reload` | Allows reloading configuration files, thresholds, and ban files safely (`/he reload`). | `op` |

---

## 5. Security & Privilege Escalation Protection

- **Tab Completion Isolation:** Tab completion filters candidates based on sender permissions. Unauthorized players cannot enumerate staff command syntax or player punishment records.
- **Self-Grant Prevention:** The `/he perm add` command explicitly forbids non-console users from granting themselves administrative permissions (`*`, `heaveneyes.*`, or nodes containing `admin`).
- **Wildcard Protection:** Root wildcard assignments (`*`) are rejected by `PermissionValidator` to maintain permission safety.
