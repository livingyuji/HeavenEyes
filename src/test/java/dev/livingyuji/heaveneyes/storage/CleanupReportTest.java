package dev.livingyuji.heaveneyes.storage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CleanupReportTest {

    @Test
    public void testCleanupReportCalculations() {
        CleanupManager.CleanupReport report = new CleanupManager.CleanupReport(15, 102400L, 5, 20480L);

        assertEquals(15, report.incidentsCount);
        assertEquals(102400L, report.incidentsBytes);
        assertEquals(5, report.playerRecordsCount);
        assertEquals(20480L, report.playerRecordsBytes);

        assertEquals(20, report.getTotalFiles());
        assertEquals(122880L, report.getTotalBytes());
    }
}
