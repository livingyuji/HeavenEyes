package dev.livingyuji.heaveneyes.storage;

import dev.livingyuji.heaveneyes.HeavenEyesPlugin;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Handles security audit logging to audit/audit.log.
 */
public class AuditLogger {

    private final HeavenEyesPlugin plugin;
    private final File auditFile;

    public AuditLogger(HeavenEyesPlugin plugin) {
        this.plugin = plugin;
        File auditDir = new File(plugin.getDataFolder(), "audit");
        if (!auditDir.exists()) {
            auditDir.mkdirs();
        }
        this.auditFile = new File(auditDir, "audit.log");
    }

    public void logAction(String action) {
        String timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        String logEntry = String.format("[%s] %s%n", timestamp, action);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (this) {
                try (PrintWriter writer = new PrintWriter(new FileWriter(auditFile, true))) {
                    writer.print(logEntry);
                } catch (IOException e) {
                    plugin.getLogger().warning("Failed to write audit log: " + e.getMessage());
                }
            }
        });
    }
}
