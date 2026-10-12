package dev.livingyuji.heaveneyes.model;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class MiningProfileTest {

    @Test
    public void testMiningRatios() {
        UUID uuid = UUID.randomUUID();
        MiningProfile profile = new MiningProfile(uuid);

        assertEquals(0.0, profile.getDiamondRatio());
        assertEquals(0.0, profile.getAncientDebrisRatio());

        profile.addStone(80);
        profile.addDeepslate(20);
        profile.addDiamond(10);
        profile.addAncientDebris(5);

        assertEquals(100, profile.getTotalBaseBlocks());
        assertEquals(0.10, profile.getDiamondRatio(), 0.001); // 10 / 100 = 10%
        assertEquals(0.05, profile.getAncientDebrisRatio(), 0.001); // 5 / 100 = 5%
    }

    @Test
    public void testMiningProfileSerialization() {
        UUID uuid = UUID.randomUUID();
        MiningProfile profile = new MiningProfile(uuid);
        profile.addStone(150);
        profile.addDeepslate(50);
        profile.addDiamond(25);
        profile.addAncientDebris(10);
        profile.addGold(30);
        profile.addIron(80);
        profile.addEmerald(5);
        profile.incrementSuspiciousVeins();

        Map<String, Object> map = profile.toMap();
        MiningProfile deserialized = MiningProfile.fromMap(map);

        assertEquals(profile.getUuid(), deserialized.getUuid());
        assertEquals(profile.getStoneMined(), deserialized.getStoneMined());
        assertEquals(profile.getDeepslateMined(), deserialized.getDeepslateMined());
        assertEquals(profile.getDiamondOreMined(), deserialized.getDiamondOreMined());
        assertEquals(profile.getAncientDebrisMined(), deserialized.getAncientDebrisMined());
        assertEquals(profile.getGoldOreMined(), deserialized.getGoldOreMined());
        assertEquals(profile.getIronOreMined(), deserialized.getIronOreMined());
        assertEquals(profile.getEmeraldOreMined(), deserialized.getEmeraldOreMined());
        assertEquals(profile.getSuspiciousVeinDiscoveries(), deserialized.getSuspiciousVeinDiscoveries());
    }
}
