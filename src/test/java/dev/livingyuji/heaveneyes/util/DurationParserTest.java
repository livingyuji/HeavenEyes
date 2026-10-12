package dev.livingyuji.heaveneyes.util;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class DurationParserTest {

    @Test
    public void testParseSeconds() {
        assertEquals(60_000L, DurationParser.parseMillis("60s"));
        assertEquals(15_000L, DurationParser.parseMillis("15sec"));
    }

    @Test
    public void testParseMinutes() {
        assertEquals(1_800_000L, DurationParser.parseMillis("30m"));
        assertEquals(300_000L, DurationParser.parseMillis("5min"));
    }

    @Test
    public void testParseHours() {
        assertEquals(3_600_000L, DurationParser.parseMillis("1h"));
        assertEquals(86_400_000L, DurationParser.parseMillis("24h"));
    }

    @Test
    public void testParseDaysAndWeeks() {
        assertEquals(604_800_000L, DurationParser.parseMillis("7d"));
        assertEquals(604_800_000L, DurationParser.parseMillis("1w"));
    }

    @Test
    public void testParseComposite() {
        assertEquals(3_600_000L + 1_800_000L, DurationParser.parseMillis("1h 30m"));
    }

    @Test
    public void testInvalidFormats() {
        assertThrows(IllegalArgumentException.class, () -> DurationParser.parseMillis(null));
        assertThrows(IllegalArgumentException.class, () -> DurationParser.parseMillis(""));
        assertThrows(IllegalArgumentException.class, () -> DurationParser.parseMillis("   "));
        assertThrows(IllegalArgumentException.class, () -> DurationParser.parseMillis("abc"));
        assertThrows(IllegalArgumentException.class, () -> DurationParser.parseMillis("-5m"));
    }

    @Test
    public void testFormatDuration() {
        assertEquals("30m", DurationParser.format(1_800_000L));
        assertEquals("1h", DurationParser.format(3_600_000L));
        assertEquals("1d 2h", DurationParser.format(86_400_000L + 7_200_000L));
        assertEquals("0s", DurationParser.format(0L));
    }
}
