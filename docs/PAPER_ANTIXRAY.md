# PAPER ANTI-XRAY CONFIGURATION GUIDE — HEAVEN EYES

**Author:** LivingYuji  
**Target Platform:** Paper 1.21.x  

---

## 1. Prevention vs. Behavioral Analysis

An effective server security strategy recognizes the fundamental difference between **prevention** and **detection**:

1. **Paper Anti-Xray (Packet Prevention):** PaperMC includes a built-in packet-level Anti-Xray system that scrambles or hides unexposed ore blocks before sending chunk data to the Minecraft client. This prevents client-side X-ray texture packs and hack clients from observing where ores are located.
2. **Heaven Eyes (Behavioral Mining Analytics):** Heaven Eyes observes actual player block-breaking events on the server side (`/he mining <player>`). It tracks mining ratios (ore broken vs. base stone broken), identifies suspicious tunnel paths toward valuable veins, and flags statistical anomalies for staff review.

> [!IMPORTANT]
> A custom plugin running on the server cannot reconstruct what a player sees on their client monitor. To achieve maximum protection against X-ray, configure Paper's built-in Anti-Xray alongside Heaven Eyes.

---

## 2. Recommended Paper Anti-Xray Configuration

To configure Paper's built-in Anti-Xray on Paper 1.21.x, edit your server's configuration file:

`config/paper-world-defaults.yml` (or per-world configurations in `worlds/<world>/paper-world.yml`):

```yaml
anticheat:
  anti-xray:
    enabled: true
    engine-mode: 2
    max-block-height: 64
    update-radius: 2
    use-permission: false
    hidden-blocks:
      - copper_ore
      - deepslate_copper_ore
      - raw_copper_block
      - diamond_ore
      - deepslate_diamond_ore
      - gold_ore
      - deepslate_gold_ore
      - iron_ore
      - deepslate_iron_ore
      - lapis_ore
      - deepslate_lapis_ore
      - redstone_ore
      - deepslate_redstone_ore
      - ancient_debris
    replacement-blocks:
      - stone
      - deepslate
      - tuff
      - diorite
      - andesite
      - granite
```

### Explanation of Engine Modes
- **Engine Mode 1:** Hides ores that are completely enclosed by solid blocks and replaces them with air/stone. Very lightweight, but uncovers ores if an air pocket is adjacent.
- **Engine Mode 2 (Recommended):** Obfuscates both unexposed blocks and fake ores into the world chunk packets. Clients see a random assortment of fake ores until they dig adjacent to the block, completely neutralizing X-ray and Freecam chunk viewers.

---

## 3. Synergy with Heaven Eyes Mining Analytics

When Paper Anti-Xray `engine-mode: 2` is enabled:
- Hacked clients cannot see ores through walls.
- Players digging directly into diamond veins without branching will exhibit abnormal ratios.
- Staff can inspect suspicious miners anytime using:
  ```text
  /he mining <player>
  ```
- Any player with an ore-to-stone ratio exceeding the configured threshold (default 18% diamonds over 60+ base blocks) will be flagged in staff review mode for administrator investigation.
