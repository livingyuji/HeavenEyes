# EVIDENCE STORAGE & PRIVACY SPECIFICATION — HEAVEN EYES

**Author:** LivingYuji  
**Platform:** Paper 1.21.x  

---

## 1. Directory Structure

Heaven Eyes persists all evidence and player histories across restarts using the following folder hierarchy:

```text
plugins/HeavenEyes/
├── bans/
│   ├── active.yml             # Active temporary and permanent bans
│   └── history.yml            # Historical, expired, and pardoned bans
├── data/
│   └── players/
│       └── <player-uuid>/
│           ├── profile.yml    # Join times, total violations, totals
│           ├── warnings.yml   # Active warning entries with timestamps
│           ├── evidence.yml   # Index of incident IDs for this player
│           └── mining.yml     # Broken stone, deepslate, and ore tallies
├── evidence/
│   └── incidents/
│       └── <incident-id>.yml  # Detailed incident evidence file
├── audit/
│   └── audit.log              # Administrative action audit trails
├── backups/                   # Compressed ZIP snapshots
└── exports/                   # Formatted player audit files
```

---

## 2. Incident Schema (`evidence/incidents/<incident-id>.yml`)

For every confirmed warning, severe detection, or automated ban, Heaven Eyes writes an incident file containing:

```yaml
incident-id: "INC-A8F2K9"
player-uuid: "b543d410-1a2f-4e02-98e3-5f21226bb854"
player-name: "BadActor"
check-name: "Speed"
violation-level: 5
confidence: 0.94
timestamp: 1776074400000
world: "world"
x: 142.34
y: 68.00
z: -520.12
explanation: "Horizontal speed 0.485 exceeded max allowed 0.360"
diagnostic-data:
  distance: 0.485
  max_allowed: 0.360
  excess: 0.125
  sprinting: true
action-triggered: "WARN_THEN_TEMPBAN"
ban-id: "HE-39F8A1"
```

---

## 3. Privacy & Data Minimization Policy

Heaven Eyes strictly adheres to server-side data minimization:
1. **No Sensitive Player Data:** Heaven Eyes never logs passwords, IP addresses, authentication tokens, or private chat messages.
2. **UUID Primary Keying:** Records are keyed by Minecraft Player UUID to ensure data integrity across username changes.
3. **Retention & Expiration:** Warnings expire automatically after `punishments.warning-reset-after` (default 24h). Incidents older than `settings.retention-days` (default 30 days) can be reviewed and purged via `/he cleanup run`.
