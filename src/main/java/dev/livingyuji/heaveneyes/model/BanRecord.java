package dev.livingyuji.heaveneyes.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a persistent ban record in Heaven Eyes.
 */
public class BanRecord {

    public enum Type {
        TEMPORARY,
        PERMANENT
    }

    public enum Source {
        MANUAL,
        AUTOMATIC
    }

    private final String banId;
    private final UUID playerUuid;
    private String playerName;
    private final Type type;
    private String reason;
    private final Source source;
    private CheckType triggeringCheck;
    private final long createdAt;
    private long expiresAt;
    private String actor;
    private String evidenceId;
    private boolean active;
    private long unbannedAt;
    private String unbannedBy;
    private String unbanReason;

    public BanRecord(String banId, UUID playerUuid, String playerName, Type type, String reason,
                     Source source, CheckType triggeringCheck, long createdAt, long expiresAt,
                     String actor, String evidenceId) {
        this.banId = banId;
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.type = type;
        this.reason = reason;
        this.source = source;
        this.triggeringCheck = triggeringCheck;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.actor = actor;
        this.evidenceId = evidenceId;
        this.active = true;
        this.unbannedAt = 0;
        this.unbannedBy = null;
        this.unbanReason = null;
    }

    public boolean isExpired() {
        if (!active) {
            return true;
        }
        if (type == Type.PERMANENT) {
            return false;
        }
        return System.currentTimeMillis() >= expiresAt;
    }

    public long getRemainingMillis() {
        if (!active || type == Type.PERMANENT) {
            return 0;
        }
        long diff = expiresAt - System.currentTimeMillis();
        return Math.max(0, diff);
    }

    public void pardon(String actor, String reason) {
        this.active = false;
        this.unbannedAt = System.currentTimeMillis();
        this.unbannedBy = actor;
        this.unbanReason = reason;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("ban-id", banId);
        map.put("player-uuid", playerUuid.toString());
        map.put("player-name", playerName);
        map.put("type", type.name());
        map.put("reason", reason);
        map.put("source", source.name());
        map.put("triggering-check", triggeringCheck != null ? triggeringCheck.name() : null);
        map.put("created-at", createdAt);
        map.put("expires-at", expiresAt);
        map.put("actor", actor);
        map.put("evidence-id", evidenceId);
        map.put("active", active);
        map.put("unbanned-at", unbannedAt);
        map.put("unbanned-by", unbannedBy);
        map.put("unban-reason", unbanReason);
        return map;
    }

    public static BanRecord fromMap(Map<?, ?> map) {
        String banId = (String) map.get("ban-id");
        UUID playerUuid = UUID.fromString((String) map.get("player-uuid"));
        String playerName = (String) map.get("player-name");
        Type type = Type.valueOf((String) map.get("type"));
        String reason = (String) map.get("reason");
        Source source = Source.valueOf((String) map.get("source"));

        String checkStr = (String) map.get("triggering-check");
        CheckType checkType = checkStr != null ? CheckType.fromString(checkStr) : null;

        long createdAt = ((Number) map.get("created-at")).longValue();
        long expiresAt = ((Number) map.get("expires-at")).longValue();
        String actor = (String) map.get("actor");
        String evidenceId = (String) map.get("evidence-id");

        BanRecord record = new BanRecord(banId, playerUuid, playerName, type, reason, source,
                checkType, createdAt, expiresAt, actor, evidenceId);

        if (map.containsKey("active")) {
            record.active = (Boolean) map.get("active");
        }
        if (map.containsKey("unbanned-at") && map.get("unbanned-at") != null) {
            record.unbannedAt = ((Number) map.get("unbanned-at")).longValue();
        }
        if (map.containsKey("unbanned-by")) {
            record.unbannedBy = (String) map.get("unbanned-by");
        }
        if (map.containsKey("unban-reason")) {
            record.unbanReason = (String) map.get("unban-reason");
        }

        return record;
    }

    // Getters and Setters
    public String getBanId() { return banId; }
    public UUID getPlayerUuid() { return playerUuid; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public Type getType() { return type; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Source getSource() { return source; }
    public CheckType getTriggeringCheck() { return triggeringCheck; }
    public long getCreatedAt() { return createdAt; }
    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }
    public String getActor() { return actor; }
    public String getEvidenceId() { return evidenceId; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public long getUnbannedAt() { return unbannedAt; }
    public String getUnbannedBy() { return unbannedBy; }
    public String getUnbanReason() { return unbanReason; }
}
