package dev.livingyuji.heaveneyes.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a persistent warning issued to a player.
 */
public class WarningRecord {

    private final UUID playerUuid;
    private final int count;
    private final CheckType checkType;
    private final long timestamp;
    private final String evidenceId;
    private final String reason;

    public WarningRecord(UUID playerUuid, int count, CheckType checkType, long timestamp, String evidenceId, String reason) {
        this.playerUuid = playerUuid;
        this.count = count;
        this.checkType = checkType;
        this.timestamp = timestamp;
        this.evidenceId = evidenceId;
        this.reason = reason;
    }

    public UUID getPlayerUuid() { return playerUuid; }
    public int getCount() { return count; }
    public CheckType getCheckType() { return checkType; }
    public long getTimestamp() { return timestamp; }
    public String getEvidenceId() { return evidenceId; }
    public String getReason() { return reason; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("player-uuid", playerUuid.toString());
        map.put("count", count);
        map.put("check-type", checkType != null ? checkType.name() : null);
        map.put("timestamp", timestamp);
        map.put("evidence-id", evidenceId);
        map.put("reason", reason);
        return map;
    }

    public static WarningRecord fromMap(Map<?, ?> map) {
        UUID playerUuid = UUID.fromString((String) map.get("player-uuid"));
        int count = ((Number) map.get("count")).intValue();
        String checkStr = (String) map.get("check-type");
        CheckType checkType = checkStr != null ? CheckType.fromString(checkStr) : null;
        long timestamp = ((Number) map.get("timestamp")).longValue();
        String evidenceId = (String) map.get("evidence-id");
        String reason = (String) map.get("reason");
        return new WarningRecord(playerUuid, count, checkType, timestamp, evidenceId, reason);
    }
}
