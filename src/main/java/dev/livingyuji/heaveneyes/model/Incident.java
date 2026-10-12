package dev.livingyuji.heaveneyes.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Persisted evidence file representation stored under evidence/incidents/<incident-id>.yml.
 */
public class Incident {

    private final String incidentId;
    private final UUID playerUuid;
    private final String playerName;
    private final String checkName;
    private final int violationLevel;
    private final double confidence;
    private final long timestamp;
    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final String explanation;
    private final Map<String, Object> diagnosticData;
    private final String actionTriggered;
    private final String banId;

    public Incident(String incidentId, UUID playerUuid, String playerName, String checkName,
                    int violationLevel, double confidence, long timestamp, String world,
                    double x, double y, double z, String explanation,
                    Map<String, Object> diagnosticData, String actionTriggered, String banId) {
        this.incidentId = incidentId;
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.checkName = checkName;
        this.violationLevel = violationLevel;
        this.confidence = confidence;
        this.timestamp = timestamp;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.explanation = explanation;
        this.diagnosticData = diagnosticData != null ? diagnosticData : new LinkedHashMap<>();
        this.actionTriggered = actionTriggered;
        this.banId = banId;
    }

    public String getIncidentId() { return incidentId; }
    public UUID getPlayerUuid() { return playerUuid; }
    public String getPlayerName() { return playerName; }
    public String getCheckName() { return checkName; }
    public int getViolationLevel() { return violationLevel; }
    public double getConfidence() { return confidence; }
    public long getTimestamp() { return timestamp; }
    public String getWorld() { return world; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public String getExplanation() { return explanation; }
    public Map<String, Object> getDiagnosticData() { return diagnosticData; }
    public String getActionTriggered() { return actionTriggered; }
    public String getBanId() { return banId; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("incident-id", incidentId);
        map.put("player-uuid", playerUuid.toString());
        map.put("player-name", playerName);
        map.put("check-name", checkName);
        map.put("violation-level", violationLevel);
        map.put("confidence", confidence);
        map.put("timestamp", timestamp);
        map.put("world", world);
        map.put("x", x);
        map.put("y", y);
        map.put("z", z);
        map.put("explanation", explanation);
        map.put("diagnostic-data", diagnosticData);
        map.put("action-triggered", actionTriggered);
        map.put("ban-id", banId);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static Incident fromMap(Map<?, ?> map) {
        String incidentId = (String) map.get("incident-id");
        UUID playerUuid = UUID.fromString((String) map.get("player-uuid"));
        String playerName = (String) map.get("player-name");
        String checkName = (String) map.get("check-name");
        int violationLevel = ((Number) map.get("violation-level")).intValue();
        double confidence = ((Number) map.get("confidence")).doubleValue();
        long timestamp = ((Number) map.get("timestamp")).longValue();
        String world = (String) map.get("world");
        double x = ((Number) map.get("x")).doubleValue();
        double y = ((Number) map.get("y")).doubleValue();
        double z = ((Number) map.get("z")).doubleValue();
        String explanation = (String) map.get("explanation");
        Map<String, Object> diagnosticData = (Map<String, Object>) map.get("diagnostic-data");
        String actionTriggered = (String) map.get("action-triggered");
        String banId = (String) map.get("ban-id");

        return new Incident(incidentId, playerUuid, playerName, checkName, violationLevel,
                confidence, timestamp, world, x, y, z, explanation, diagnosticData, actionTriggered, banId);
    }
}
