package dev.livingyuji.heaveneyes.model;

/**
 * Enumeration of all anti-cheat detection modules in Heaven Eyes.
 */
public enum CheckType {
    // Movement checks
    SPEED("Speed", CheckCategory.MOVEMENT, "Abnormal horizontal movement speed"),
    FLIGHT("Flight", CheckCategory.MOVEMENT, "Unsupported vertical elevation and floating"),
    NO_FALL("NoFall", CheckCategory.MOVEMENT, "Spoofed on-ground status during fall"),
    TIMER("Timer", CheckCategory.MOVEMENT, "Excessive movement tick rate"),
    STEP("Step", CheckCategory.MOVEMENT, "Abnormal vertical step height"),
    VELOCITY("Velocity", CheckCategory.MOVEMENT, "Invalid velocity and knockback response"),

    // Combat checks
    REACH("Reach", CheckCategory.COMBAT, "Attack distance exceeding legitimate range"),
    KILL_AURA("KillAura", CheckCategory.COMBAT, "Attack angle or multi-target anomaly"),
    FAST_CLICK("FastClick", CheckCategory.COMBAT, "Click rate exceeding human limit"),
    AIM_SNAP("AimSnap", CheckCategory.COMBAT, "Instantaneous aim rotation snap"),
    CRITICALS("Criticals", CheckCategory.COMBAT, "Critical hit generated without falling"),

    // Mining heuristics
    MINING_ANALYTICS("MiningAnalytics", CheckCategory.MINING, "Abnormal ore ratio or direct vein route"),

    // ESP heuristics
    CONTAINER_ESP("ContainerESP", CheckCategory.ESP, "Container interaction through occlusion"),
    FREECAM("Freecam", CheckCategory.FREECAM, "Interaction detached from player position");

    private final String identifier;
    private final CheckCategory category;
    private final String description;

    CheckType(String identifier, CheckCategory category, String description) {
        this.identifier = identifier;
        this.category = category;
        this.description = description;
    }

    public String getIdentifier() {
        return identifier;
    }

    public CheckCategory getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public static CheckType fromString(String name) {
        if (name == null) {
            return null;
        }
        String clean = name.trim().replace("-", "_").toUpperCase();
        for (CheckType type : values()) {
            if (type.name().equalsIgnoreCase(clean) || type.identifier.equalsIgnoreCase(name.trim())) {
                return type;
            }
        }
        return null;
    }
}
