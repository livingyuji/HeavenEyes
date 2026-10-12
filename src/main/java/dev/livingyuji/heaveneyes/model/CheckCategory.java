package dev.livingyuji.heaveneyes.model;

/**
 * High-level categories for Heaven Eyes checks.
 */
public enum CheckCategory {
    MOVEMENT("Movement"),
    COMBAT("Combat"),
    MINING("Mining"),
    ESP("ESP"),
    FREECAM("Freecam");

    private final String displayName;

    CheckCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
