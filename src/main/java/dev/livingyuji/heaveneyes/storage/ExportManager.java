package dev.livingyuji.heaveneyes.storage;

import dev.livingyuji.heaveneyes.HeavenEyesPlugin;
import dev.livingyuji.heaveneyes.model.BanRecord;
import dev.livingyuji.heaveneyes.model.MiningProfile;
import dev.livingyuji.heaveneyes.model.PlayerProfile;
import dev.livingyuji.heaveneyes.model.WarningRecord;
import dev.livingyuji.heaveneyes.util.DurationParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Exports detailed human-readable player audit summaries to exports/<player>_<timestamp>.txt.
 */
public class ExportManager {

    private final HeavenEyesPlugin plugin;
    private final File exportsDir;

    public ExportManager(HeavenEyesPlugin plugin) {
        this.plugin = plugin;
        this.exportsDir = new File(plugin.getDataFolder(), "exports");
    }

    public void initialize() {
        if (!exportsDir.exists()) {
            exportsDir.mkdirs();
        }
    }

    public CompletableFuture<File> exportPlayerReportAsync(UUID uuid, String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String fileName = playerName + "_" + sdf.format(new Date()) + ".txt";
            File exportFile = new File(exportsDir, fileName);

            PlayerProfile profile = plugin.getPlayerDataManager().getPlayerProfile(uuid, playerName);
            MiningProfile mining = plugin.getPlayerDataManager().getMiningProfile(uuid);
            List<WarningRecord> warnings = plugin.getWarningManager().getActiveWarnings(uuid);
            List<BanRecord> banHistory = plugin.getBanManager().getPlayerBanHistory(uuid);
            List<String> evidenceList = plugin.getPlayerDataManager().getPlayerEvidenceList(uuid);

            try (PrintWriter writer = new PrintWriter(new FileWriter(exportFile))) {
                writer.println("============================================================");
                writer.println("         HEAVEN EYES — PLAYER AUDIT & EVIDENCE REPORT       ");
                writer.println("============================================================");
                writer.println("Generated: " + new Date());
                writer.println("Player Name: " + playerName);
                writer.println("UUID: " + uuid);
                writer.println("Exempt Status: " + (profile != null && profile.isExempt()));
                writer.println();

                writer.println("--- PROFILE STATISTICS ---");
                if (profile != null) {
                    writer.println("First Seen: " + new Date(profile.getFirstSeen()));
                    writer.println("Last Seen: " + new Date(profile.getLastSeen()));
                    writer.println("Total Violations: " + profile.getTotalViolations());
                    writer.println("Total Warnings Issued: " + profile.getTotalWarnings());
                    writer.println("Total Bans: " + profile.getTotalBans());
                } else {
                    writer.println("No persistent profile found.");
                }
                writer.println();

                writer.println("--- ACTIVE WARNINGS (" + warnings.size() + ") ---");
                for (WarningRecord w : warnings) {
                    writer.printf("- Warning #%d: Check=%s, Reason='%s', Evidence=%s, Time=%s%n",
                            w.getCount(), w.getCheckType().getIdentifier(), w.getReason(),
                            w.getEvidenceId(), new Date(w.getTimestamp()));
                }
                writer.println();

                writer.println("--- BAN HISTORY (" + banHistory.size() + ") ---");
                for (BanRecord b : banHistory) {
                    writer.printf("- Ban ID: %s [%s] Type=%s, Reason='%s', Actor=%s, Active=%s%n",
                            b.getBanId(), b.getSource(), b.getType(), b.getReason(), b.getActor(), b.isActive());
                    if (b.getExpiresAt() > 0) {
                        writer.printf("  Expires: %s (Remaining: %s)%n", new Date(b.getExpiresAt()),
                                DurationParser.format(b.getRemainingMillis()));
                    }
                    if (!b.isActive() && b.getUnbannedBy() != null) {
                        writer.printf("  Pardoned: by %s, reason='%s', at %s%n",
                                b.getUnbannedBy(), b.getUnbanReason(), new Date(b.getUnbannedAt()));
                    }
                }
                writer.println();

                writer.println("--- MINING ANALYTICS ---");
                if (mining != null) {
                    writer.println("Stone Broken: " + mining.getStoneMined());
                    writer.println("Deepslate Broken: " + mining.getDeepslateMined());
                    writer.printf("Diamonds Mined: %d (Ratio: %.2f%%)%n", mining.getDiamondOreMined(), mining.getDiamondRatio() * 100.0);
                    writer.printf("Ancient Debris Mined: %d (Ratio: %.2f%%)%n", mining.getAncientDebrisMined(), mining.getAncientDebrisRatio() * 100.0);
                    writer.println("Gold Mined: " + mining.getGoldOreMined());
                    writer.println("Iron Mined: " + mining.getIronOreMined());
                    writer.println("Suspicious Veins Flagged: " + mining.getSuspiciousVeinDiscoveries());
                }
                writer.println();

                writer.println("--- RECORDED INCIDENT EVIDENCE REFERENCES (" + evidenceList.size() + ") ---");
                for (String ev : evidenceList) {
                    writer.println("- " + ev);
                }
                writer.println();
                writer.println("============================================================");

                plugin.getAuditLogger().logAction(String.format("EXPORT_GENERATED player=%s(%s) file=%s", playerName, uuid, fileName));
                return exportFile;
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to export player report: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
}
