package dev.livingyuji.heaveneyes.model;

/**
 * Action policy applied when a check triggers automated handling.
 */
public enum CheckAction {
    /**
     * Standard graduated punishment: 3 warnings leading to a 30m tempban.
     */
    WARN_THEN_TEMPBAN,

    /**
     * Flagged for staff review and evidence collection only. No automatic bans.
     */
    REVIEW,

    /**
     * Immediate disconnection/kick on confirmed severe violation.
     */
    KICK,

    /**
     * Direct ban action for high-severity violations.
     */
    BAN,

    /**
     * Silent recording or monitoring only.
     */
    NONE;

    public static CheckAction fromString(String name, CheckAction defaultAction) {
        if (name == null || name.trim().isEmpty()) {
            return defaultAction;
        }
        try {
            return CheckAction.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return defaultAction;
        }
    }
}
