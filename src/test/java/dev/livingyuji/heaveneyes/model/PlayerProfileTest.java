package dev.livingyuji.heaveneyes.model;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerProfileTest {

    @Test
    public void testPlayerProfileProgression() {
        UUID uuid = UUID.randomUUID();
        PlayerProfile profile = new PlayerProfile(uuid, "TestUser");

        assertEquals(0, profile.getTotalViolations());
        assertEquals(0, profile.getTotalWarnings());
        assertEquals(0, profile.getTotalBans());
        assertFalse(profile.isExempt());

        profile.incrementViolations();
        profile.incrementViolations();
        profile.incrementWarnings();
        profile.incrementBans();
        profile.setExempt(true);

        assertEquals(2, profile.getTotalViolations());
        assertEquals(1, profile.getTotalWarnings());
        assertEquals(1, profile.getTotalBans());
        assertTrue(profile.isExempt());
    }

    @Test
    public void testProfileSerializationRoundtrip() {
        UUID uuid = UUID.randomUUID();
        PlayerProfile profile = new PlayerProfile(uuid, "Steve");
        profile.incrementViolations();
        profile.incrementWarnings();
        profile.incrementBans();
        profile.setExempt(true);

        Map<String, Object> map = profile.toMap();
        PlayerProfile deserialized = PlayerProfile.fromMap(map);

        assertEquals(profile.getUuid(), deserialized.getUuid());
        assertEquals(profile.getLastKnownName(), deserialized.getLastKnownName());
        assertEquals(profile.getTotalViolations(), deserialized.getTotalViolations());
        assertEquals(profile.getTotalWarnings(), deserialized.getTotalWarnings());
        assertEquals(profile.getTotalBans(), deserialized.getTotalBans());
        assertEquals(profile.isExempt(), deserialized.isExempt());
    }
}
