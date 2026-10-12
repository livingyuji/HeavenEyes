package dev.livingyuji.heaveneyes.punishment;

import dev.livingyuji.heaveneyes.model.BanRecord;
import dev.livingyuji.heaveneyes.model.CheckType;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class BanRecordTest {

    @Test
    public void testPermanentBanExpiry() {
        UUID uuid = UUID.randomUUID();
        BanRecord ban = new BanRecord(
                "HE-TEST01",
                uuid,
                "BadActor",
                BanRecord.Type.PERMANENT,
                "Cheating",
                BanRecord.Source.AUTOMATIC,
                CheckType.SPEED,
                System.currentTimeMillis(),
                0,
                "HeavenEyes",
                "INC-001"
        );

        assertFalse(ban.isExpired());
        assertTrue(ban.isActive());
        assertEquals(0L, ban.getRemainingMillis());
    }

    @Test
    public void testTemporaryBanExpiry() {
        UUID uuid = UUID.randomUUID();
        long now = System.currentTimeMillis();
        long expiresAt = now + 60_000L; // 1 minute in future

        BanRecord ban = new BanRecord(
                "HE-TEST02",
                uuid,
                "Speedy",
                BanRecord.Type.TEMPORARY,
                "Speed violation",
                BanRecord.Source.AUTOMATIC,
                CheckType.SPEED,
                now,
                expiresAt,
                "HeavenEyes",
                "INC-002"
        );

        assertFalse(ban.isExpired());
        assertTrue(ban.getRemainingMillis() > 0);

        // Test with past expiry
        ban.setExpiresAt(now - 1000L);
        assertTrue(ban.isExpired());
        assertEquals(0L, ban.getRemainingMillis());
    }

    @Test
    public void testPardon() {
        UUID uuid = UUID.randomUUID();
        BanRecord ban = new BanRecord(
                "HE-TEST03",
                uuid,
                "PardonMe",
                BanRecord.Type.PERMANENT,
                "Speed violation",
                BanRecord.Source.MANUAL,
                null,
                System.currentTimeMillis(),
                0,
                "Admin",
                null
        );

        assertTrue(ban.isActive());
        ban.pardon("SeniorMod", "Appeal accepted");

        assertFalse(ban.isActive());
        assertTrue(ban.isExpired());
        assertEquals("SeniorMod", ban.getUnbannedBy());
        assertEquals("Appeal accepted", ban.getUnbanReason());
        assertTrue(ban.getUnbannedAt() > 0);
    }

    @Test
    public void testMapSerializationRoundtrip() {
        UUID uuid = UUID.randomUUID();
        long now = System.currentTimeMillis();
        long expires = now + 1_800_000L;

        BanRecord original = new BanRecord(
                "HE-XYZ123",
                uuid,
                "CheaterBob",
                BanRecord.Type.TEMPORARY,
                "KillAura detected",
                BanRecord.Source.AUTOMATIC,
                CheckType.KILL_AURA,
                now,
                expires,
                "HeavenEyes",
                "INC-999"
        );

        Map<String, Object> map = original.toMap();
        BanRecord deserialized = BanRecord.fromMap(map);

        assertEquals(original.getBanId(), deserialized.getBanId());
        assertEquals(original.getPlayerUuid(), deserialized.getPlayerUuid());
        assertEquals(original.getPlayerName(), deserialized.getPlayerName());
        assertEquals(original.getType(), deserialized.getType());
        assertEquals(original.getReason(), deserialized.getReason());
        assertEquals(original.getSource(), deserialized.getSource());
        assertEquals(original.getTriggeringCheck(), deserialized.getTriggeringCheck());
        assertEquals(original.getCreatedAt(), deserialized.getCreatedAt());
        assertEquals(original.getExpiresAt(), deserialized.getExpiresAt());
        assertEquals(original.getActor(), deserialized.getActor());
        assertEquals(original.getEvidenceId(), deserialized.getEvidenceId());
        assertEquals(original.isActive(), deserialized.isActive());
    }
}
