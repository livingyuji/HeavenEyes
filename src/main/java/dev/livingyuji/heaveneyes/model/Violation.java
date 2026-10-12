package dev.livingyuji.heaveneyes.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a single anti-cheat detection violation event.
 */
public class Violation {

    private final UUID playerUuid;
    private final String playerName;
    private final CheckType checkType;
    private final int violationLevel;
    private final double confidence;
    private final long timestamp;
    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final String explanation;
    private final Map<String, Object> diagnosticData;

    public Violation(UUID playerUuid, String playerName, CheckType checkType, int violationLevel,
                     double confidence, long timestamp, String world, double x, double y, double z,
                     String explanation, Map<String, Object> diagnosticData) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.checkType = checkType;
        this.violationLevel = violationLevel;
        this.confidence = confidence;
        this.timestamp = timestamp;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.explanation = explanation;
        this.diagnosticData = diagnosticData != null ? new LinkedHashMap<>(diagnosticData) : Collections.emptyMap();
    }

    public UUID getPlayerUuid() { return playerUuid; }
    public String getPlayerName() { return playerName; }
    public CheckType getCheckType() { return checkType; }
    public int getViolationLevel() { return violationLevel; }
    public double getConfidence() { return confidence; }
    public long getTimestamp() { return timestamp; }
    public String getWorld() { return world; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public String getExplanation() { return explanation; }
    public Map<String, Object> getDiagnosticData() { return diagnosticData; }
}
