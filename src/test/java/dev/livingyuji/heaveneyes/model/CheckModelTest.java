package dev.livingyuji.heaveneyes.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CheckModelTest {

    @Test
    public void testCheckTypeParsing() {
        assertEquals(CheckType.SPEED, CheckType.fromString("Speed"));
        assertEquals(CheckType.SPEED, CheckType.fromString("speed"));
        assertEquals(CheckType.KILL_AURA, CheckType.fromString("KillAura"));
        assertEquals(CheckType.KILL_AURA, CheckType.fromString("kill_aura"));
        assertEquals(CheckType.KILL_AURA, CheckType.fromString("kill-aura"));
        assertEquals(CheckType.MINING_ANALYTICS, CheckType.fromString("MiningAnalytics"));
        assertNull(CheckType.fromString("NonExistentCheck"));
        assertNull(CheckType.fromString(null));
    }

    @Test
    public void testCheckActionParsing() {
        assertEquals(CheckAction.WARN_THEN_TEMPBAN, CheckAction.fromString("WARN_THEN_TEMPBAN", CheckAction.NONE));
        assertEquals(CheckAction.REVIEW, CheckAction.fromString("review", CheckAction.NONE));
        assertEquals(CheckAction.KICK, CheckAction.fromString("KICK", CheckAction.NONE));
        assertEquals(CheckAction.BAN, CheckAction.fromString("ban", CheckAction.NONE));
        assertEquals(CheckAction.NONE, CheckAction.fromString("unknown", CheckAction.NONE));
        assertEquals(CheckAction.REVIEW, CheckAction.fromString(null, CheckAction.REVIEW));
    }

    @Test
    public void testCategories() {
        assertEquals(CheckCategory.MOVEMENT, CheckType.SPEED.getCategory());
        assertEquals(CheckCategory.COMBAT, CheckType.REACH.getCategory());
        assertEquals(CheckCategory.MINING, CheckType.MINING_ANALYTICS.getCategory());
        assertEquals(CheckCategory.ESP, CheckType.CONTAINER_ESP.getCategory());
        assertEquals(CheckCategory.FREECAM, CheckType.FREECAM.getCategory());
    }
}
