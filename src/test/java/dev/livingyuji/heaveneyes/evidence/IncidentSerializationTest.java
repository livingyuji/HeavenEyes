package dev.livingyuji.heaveneyes.evidence;

import dev.livingyuji.heaveneyes.model.Incident;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class IncidentSerializationTest {

    @Test
    public void testIncidentRoundtrip() {
        UUID uuid = UUID.randomUUID();
        Map<String, Object> diag = new HashMap<>();
        diag.put("reach_distance", 4.12);
        diag.put("max_reach", 3.8);
        diag.put("ping", 45);

        Incident incident = new Incident(
                "INC-ABC789",
                uuid,
                "Attacker99",
                "Reach",
                5,
                0.96,
                System.currentTimeMillis(),
                "world",
                100.5,
                64.0,
                -200.5,
                "Reach 4.12 exceeded 3.80",
                diag,
                "WARN_THEN_TEMPBAN",
                "HE-BAN123"
        );

        Map<String, Object> map = incident.toMap();
        Incident deserialized = Incident.fromMap(map);

        assertEquals(incident.getIncidentId(), deserialized.getIncidentId());
        assertEquals(incident.getPlayerUuid(), deserialized.getPlayerUuid());
        assertEquals(incident.getPlayerName(), deserialized.getPlayerName());
        assertEquals(incident.getCheckName(), deserialized.getCheckName());
        assertEquals(incident.getViolationLevel(), deserialized.getViolationLevel());
        assertEquals(incident.getConfidence(), deserialized.getConfidence(), 0.001);
        assertEquals(incident.getWorld(), deserialized.getWorld());
        assertEquals(incident.getX(), deserialized.getX(), 0.001);
        assertEquals(incident.getY(), deserialized.getY(), 0.001);
        assertEquals(incident.getZ(), deserialized.getZ(), 0.001);
        assertEquals(incident.getExplanation(), deserialized.getExplanation());
        assertEquals(incident.getActionTriggered(), deserialized.getActionTriggered());
        assertEquals(incident.getBanId(), deserialized.getBanId());
        assertEquals(3, deserialized.getDiagnosticData().size());
    }
}
