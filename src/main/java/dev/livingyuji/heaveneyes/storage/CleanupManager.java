package dev.livingyuji.heaveneyes.storage;

import dev.livingyuji.heaveneyes.HeavenEyesPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages data retention evaluation, previewing, and execution of cleanup runs.
 */
public class CleanupManager {

    private final HeavenEyesPlugin plugin;

    public CleanupManager(HeavenEyesPlugin plugin) {
        this.plugin = plugin;
    }

    public static class CleanupReport {
        public final int incidentsCount;
        public final long incidentsBytes;
        public final int playerRecordsCount;
        public final long playerRecordsBytes;

        public CleanupReport(int incidentsCount, long incidentsBytes, int playerRecordsCount, long playerRecordsBytes) {
            this.incidentsCount = incidentsCount;
            this.incidentsBytes = incidentsBytes;
            this.playerRecordsCount = playerRecordsCount;
            this.playerRecordsBytes = playerRecordsBytes;
        }

        public int getTotalFiles() { return incidentsCount + playerRecordsCount; }
        public long getTotalBytes() { return incidentsBytes + playerRecordsBytes; }
    }

    public CleanupReport evaluateCleanup(boolean deleteFiles) {
        long retentionDays = plugin.getPluginConfig().getRetentionDays();
        long cutoffMillis = System.currentTimeMillis() - (retentionDays * 86_400_000L);

        int incCount = 0;
        long incBytes = 0;
        File incDir = new File(plugin.getDataFolder(), "evidence/incidents");
        if (incDir.exists() && incDir.isDirectory()) {
            File[] files = incDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.lastModified() < cutoffMillis) {
                        incCount++;
                        incBytes += file.length();
                        if (deleteFiles) {
                            file.delete();
                        }
                    }
                }
            }
        }

        int playerRecCount = 0;
        long playerRecBytes = 0;
        File playersDir = new File(plugin.getDataFolder(), "data/players");
        if (playersDir.exists() && playersDir.isDirectory()) {
            File[] pDirs = playersDir.listFiles();
            if (pDirs != null) {
                for (File pDir : pDirs) {
                    if (pDir.isDirectory()) {
                        File[] pFiles = pDir.listFiles();
                        if (pFiles != null) {
                            for (File pFile : pFiles) {
                                if (pFile.isFile() && pFile.getName().equals("violations.yml") && pFile.lastModified() < cutoffMillis) {
                                    playerRecCount++;
                                    playerRecBytes += pFile.length();
                                    if (deleteFiles) {
                                        pFile.delete();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (deleteFiles) {
            plugin.getAuditLogger().logAction(String.format("CLEANUP_EXECUTED filesDeleted=%d bytesReclaimed=%d retentionDays=%d",
                    incCount + playerRecCount, incBytes + playerRecBytes, retentionDays));
        }

        return new CleanupReport(incCount, incBytes, playerRecCount, playerRecBytes);
    }
}
