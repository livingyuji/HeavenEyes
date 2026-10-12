package dev.livingyuji.heaveneyes.storage;

import dev.livingyuji.heaveneyes.HeavenEyesPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Creates compressed backups of Heaven Eyes configuration, records, and evidence.
 */
public class BackupManager {

    private final HeavenEyesPlugin plugin;
    private final File backupDir;

    public BackupManager(HeavenEyesPlugin plugin) {
        this.plugin = plugin;
        this.backupDir = new File(plugin.getDataFolder(), "backups");
    }

    public void initialize() {
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
    }

    public CompletableFuture<File> createBackupAsync() {
        return CompletableFuture.supplyAsync(() -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String archiveName = "heaveneyes_backup_" + sdf.format(new Date()) + ".zip";
            File zipFile = new File(backupDir, archiveName);

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
                zipDirectory(plugin.getDataFolder(), plugin.getDataFolder(), zos);
                plugin.getAuditLogger().logAction("BACKUP_CREATED file=" + archiveName);
                return zipFile;
            } catch (IOException e) {
                plugin.getLogger().severe("Backup creation failed: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    private void zipDirectory(File rootDir, File sourceDir, ZipOutputStream zos) throws IOException {
        File[] files = sourceDir.listFiles();
        if (files == null) return;

        byte[] buffer = new byte[8192];
        for (File file : files) {
            // Avoid recursion into backups folder itself
            if (file.getAbsolutePath().startsWith(backupDir.getAbsolutePath())) {
                continue;
            }

            if (file.isDirectory()) {
                zipDirectory(rootDir, file, zos);
            } else {
                String entryPath = rootDir.toURI().relativize(file.toURI()).getPath();
                ZipEntry entry = new ZipEntry(entryPath);
                zos.putNextEntry(entry);
                try (FileInputStream fis = new FileInputStream(file)) {
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                }
                zos.closeEntry();
            }
        }
    }
}
