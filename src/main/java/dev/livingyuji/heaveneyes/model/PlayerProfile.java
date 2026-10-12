package dev.livingyuji.heaveneyes.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Persisted profile for a player stored in data/players/<uuid>/profile.yml.
 */
public class PlayerProfile {

    private final UUID uuid;
    private String lastKnownName;
    private long firstSeen;
    private long lastSeen;
    private boolean exempt;
    private int totalViolations;
    private int totalWarnings;
    private int totalBans;

    public PlayerProfile(UUID uuid, String lastKnownName) {
        this.uuid = uuid;
        this.lastKnownName = lastKnownName;
        long now = System.currentTimeMillis();
        this.firstSeen = now;
        this.lastSeen = now;
        this.exempt = false;
        this.totalViolations = 0;
        this.totalWarnings = 0;
        this.totalBans = 0;
    }

    public UUID getUuid() { return uuid; }
    public String getLastKnownName() { return lastKnownName; }
    public void setLastKnownName(String lastKnownName) { this.lastKnownName = lastKnownName; }
    public long getFirstSeen() { return firstSeen; }
    public void setFirstSeen(long firstSeen) { this.firstSeen = firstSeen; }
    public long getLastSeen() { return lastSeen; }
    public void setLastSeen(long lastSeen) { this.lastSeen = lastSeen; }
    public boolean isExempt() { return exempt; }
    public void setExempt(boolean exempt) { this.exempt = exempt; }
    public int getTotalViolations() { return totalViolations; }
    public void incrementViolations() { this.totalViolations++; }
    public int getTotalWarnings() { return totalWarnings; }
    public void incrementWarnings() { this.totalWarnings++; }
    public int getTotalBans() { return totalBans; }
    public void incrementBans() { this.totalBans++; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("uuid", uuid.toString());
        map.put("last-known-name", lastKnownName);
        map.put("first-seen", firstSeen);
        map.put("last-seen", lastSeen);
        map.put("exempt", exempt);
        map.put("total-violations", totalViolations);
        map.put("total-warnings", totalWarnings);
        map.put("total-bans", totalBans);
        return map;
    }

    public static PlayerProfile fromMap(Map<?, ?> map) {
        UUID uuid = UUID.fromString((String) map.get("uuid"));
        String name = (String) map.get("last-known-name");
        PlayerProfile profile = new PlayerProfile(uuid, name);
        if (map.containsKey("first-seen")) {
            profile.setFirstSeen(((Number) map.get("first-seen")).longValue());
        }
        if (map.containsKey("last-seen")) {
            profile.setLastSeen(((Number) map.get("last-seen")).longValue());
        }
        if (map.containsKey("exempt")) {
            profile.setExempt((Boolean) map.get("exempt"));
        }
        if (map.containsKey("total-violations")) {
            profile.totalViolations = ((Number) map.get("total-violations")).intValue();
        }
        if (map.containsKey("total-warnings")) {
            profile.totalWarnings = ((Number) map.get("total-warnings")).intValue();
        }
        if (map.containsKey("total-bans")) {
            profile.totalBans = ((Number) map.get("total-bans")).intValue();
        }
        return profile;
    }
}
