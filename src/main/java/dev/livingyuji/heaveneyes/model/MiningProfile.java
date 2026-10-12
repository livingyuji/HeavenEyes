package dev.livingyuji.heaveneyes.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Persisted mining statistics stored under data/players/<uuid>/mining.yml.
 */
public class MiningProfile {

    private final UUID uuid;
    private long stoneMined;
    private long deepslateMined;
    private long diamondOreMined;
    private long ancientDebrisMined;
    private long goldOreMined;
    private long ironOreMined;
    private long emeraldOreMined;
    private int suspiciousVeinDiscoveries;
    private long lastMinedTimestamp;

    public MiningProfile(UUID uuid) {
        this.uuid = uuid;
        this.stoneMined = 0;
        this.deepslateMined = 0;
        this.diamondOreMined = 0;
        this.ancientDebrisMined = 0;
        this.goldOreMined = 0;
        this.ironOreMined = 0;
        this.emeraldOreMined = 0;
        this.suspiciousVeinDiscoveries = 0;
        this.lastMinedTimestamp = System.currentTimeMillis();
    }

    public UUID getUuid() { return uuid; }
    public long getStoneMined() { return stoneMined; }
    public void addStone(long amount) { this.stoneMined += amount; }
    public long getDeepslateMined() { return deepslateMined; }
    public void addDeepslate(long amount) { this.deepslateMined += amount; }
    public long getDiamondOreMined() { return diamondOreMined; }
    public void addDiamond(long amount) { this.diamondOreMined += amount; }
    public long getAncientDebrisMined() { return ancientDebrisMined; }
    public void addAncientDebris(long amount) { this.ancientDebrisMined += amount; }
    public long getGoldOreMined() { return goldOreMined; }
    public void addGold(long amount) { this.goldOreMined += amount; }
    public long getIronOreMined() { return ironOreMined; }
    public void addIron(long amount) { this.ironOreMined += amount; }
    public long getEmeraldOreMined() { return emeraldOreMined; }
    public void addEmerald(long amount) { this.emeraldOreMined += amount; }
    public int getSuspiciousVeinDiscoveries() { return suspiciousVeinDiscoveries; }
    public void incrementSuspiciousVeins() { this.suspiciousVeinDiscoveries++; }
    public long getLastMinedTimestamp() { return lastMinedTimestamp; }
    public void setLastMinedTimestamp(long timestamp) { this.lastMinedTimestamp = timestamp; }

    public long getTotalBaseBlocks() {
        return stoneMined + deepslateMined;
    }

    public double getDiamondRatio() {
        long totalBase = getTotalBaseBlocks();
        if (totalBase == 0) return 0.0;
        return (double) diamondOreMined / (double) totalBase;
    }

    public double getAncientDebrisRatio() {
        long totalBase = getTotalBaseBlocks();
        if (totalBase == 0) return 0.0;
        return (double) ancientDebrisMined / (double) totalBase;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("uuid", uuid.toString());
        map.put("stone-mined", stoneMined);
        map.put("deepslate-mined", deepslateMined);
        map.put("diamond-ore-mined", diamondOreMined);
        map.put("ancient-debris-mined", ancientDebrisMined);
        map.put("gold-ore-mined", goldOreMined);
        map.put("iron-ore-mined", ironOreMined);
        map.put("emerald-ore-mined", emeraldOreMined);
        map.put("suspicious-veins", suspiciousVeinDiscoveries);
        map.put("last-mined", lastMinedTimestamp);
        return map;
    }

    public static MiningProfile fromMap(Map<?, ?> map) {
        UUID uuid = UUID.fromString((String) map.get("uuid"));
        MiningProfile profile = new MiningProfile(uuid);
        if (map.containsKey("stone-mined")) profile.stoneMined = ((Number) map.get("stone-mined")).longValue();
        if (map.containsKey("deepslate-mined")) profile.deepslateMined = ((Number) map.get("deepslate-mined")).longValue();
        if (map.containsKey("diamond-ore-mined")) profile.diamondOreMined = ((Number) map.get("diamond-ore-mined")).longValue();
        if (map.containsKey("ancient-debris-mined")) profile.ancientDebrisMined = ((Number) map.get("ancient-debris-mined")).longValue();
        if (map.containsKey("gold-ore-mined")) profile.goldOreMined = ((Number) map.get("gold-ore-mined")).longValue();
        if (map.containsKey("iron-ore-mined")) profile.ironOreMined = ((Number) map.get("iron-ore-mined")).longValue();
        if (map.containsKey("emerald-ore-mined")) profile.emeraldOreMined = ((Number) map.get("emerald-ore-mined")).longValue();
        if (map.containsKey("suspicious-veins")) profile.suspiciousVeinDiscoveries = ((Number) map.get("suspicious-veins")).intValue();
        if (map.containsKey("last-mined")) profile.lastMinedTimestamp = ((Number) map.get("last-mined")).longValue();
        return profile;
    }
}
