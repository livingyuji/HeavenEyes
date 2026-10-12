package dev.livingyuji.heaveneyes.model;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class WarningRecordTest {

    @Test
    public void testWarningRecordProgression() {
        UUID uuid = UUID.randomUUID();
        long now = System.currentTimeMillis();

        WarningRecord w1 = new WarningRecord(uuid, 1, CheckType.SPEED, now, "INC-001", "Speed violation");
        assertEquals(uuid, w1.getPlayerUuid());
        assertEquals(1, w1.getCount());
        assertEquals(CheckType.SPEED, w1.getCheckType());
        assertEquals(now, w1.getTimestamp());
        assertEquals("INC-001", w1.getEvidenceId());
        assertEquals("Speed violation", w1.getReason());

        WarningRecord w2 = new WarningRecord(uuid, 2, CheckType.SPEED, now + 1000L, "INC-002", "Speed violation");
        assertEquals(2, w2.getCount());

        WarningRecord w3 = new WarningRecord(uuid, 3, CheckType.SPEED, now + 2000L, "INC-003", "Speed violation");
        assertEquals(3, w3.getCount());
    }

    @Test
    public void testWarningRecordSerializationRoundtrip() {
        UUID uuid = UUID.randomUUID();
        long now = System.currentTimeMillis();

        WarningRecord original = new WarningRecord(uuid, 2, CheckType.REACH, now, "INC-ABC", "Reach violation");
        Map<String, Object> map = original.toMap();
        WarningRecord deserialized = WarningRecord.fromMap(map);

        assertEquals(original.getPlayerUuid(), deserialized.getPlayerUuid());
        assertEquals(original.getCount(), deserialized.getCount());
        assertEquals(original.getCheckType(), deserialized.getCheckType());
        assertEquals(original.getTimestamp(), deserialized.getTimestamp());
        assertEquals(original.getEvidenceId(), deserialized.getEvidenceId());
        assertEquals(original.getReason(), deserialized.getReason());
    }
}
