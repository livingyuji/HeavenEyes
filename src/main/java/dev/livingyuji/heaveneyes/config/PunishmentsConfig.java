package dev.livingyuji.heaveneyes.config;

import dev.livingyuji.heaveneyes.util.DurationParser;
import org.bukkit.configuration.file.FileConfiguration;

public class PunishmentsConfig {

    private boolean automaticEnabled = true;
    private int warningsBeforeTempban = 3;
    private String tempbanDurationString = "30m";
    private long tempbanDurationMillis = 1_800_000L;
    private String warningResetString = "24h";
    private long warningResetMillis = 86_400_000L;
    private boolean saveEvidenceBeforePunishment = true;
    private String repeatedDetectionWindowString = "60s";
    private long repeatedDetectionWindowMillis = 60_000L;
    private boolean requireConfidenceThreshold = true;
    private boolean severeKickEnabled = true;
    private boolean severeTempbanEnabled = true;
    private boolean preventDuplicatePunishments = true;

    private String warningMessage = "&cHeaven Eyes &7detected suspicious activity. Warning &e{count}&7/&c{maximum}&7.";
    private String kickMessage = "&cHeaven Eyes &7removed you for severe suspicious activity.";
    private String tempbanMessage = "&cHeaven Eyes &7temporarily banned you for &e{duration}&7. Reason: &f{reason}";
    private String permbanMessage = "&cHeaven Eyes &7permanently suspended your access. Reason: &f{reason}";

    public void load(FileConfiguration config) {
        this.automaticEnabled = config.getBoolean("punishments.automatic", true);
        this.warningsBeforeTempban = config.getInt("punishments.warnings-before-tempban", 3);

        this.tempbanDurationString = config.getString("punishments.tempban-duration", "30m");
        try {
            this.tempbanDurationMillis = DurationParser.parseMillis(tempbanDurationString);
        } catch (Exception e) {
            this.tempbanDurationMillis = 1_800_000L;
        }

        this.warningResetString = config.getString("punishments.warning-reset-after", "24h");
        try {
            this.warningResetMillis = DurationParser.parseMillis(warningResetString);
        } catch (Exception e) {
            this.warningResetMillis = 86_400_000L;
        }

        this.saveEvidenceBeforePunishment = config.getBoolean("punishments.save-evidence-before-punishment", true);

        this.repeatedDetectionWindowString = config.getString("punishments.repeated-detection-window", "60s");
        try {
            this.repeatedDetectionWindowMillis = DurationParser.parseMillis(repeatedDetectionWindowString);
        } catch (Exception e) {
            this.repeatedDetectionWindowMillis = 60_000L;
        }

        this.requireConfidenceThreshold = config.getBoolean("punishments.require-confidence-threshold", true);
        this.severeKickEnabled = config.getBoolean("punishments.severe-kick-enabled", true);
        this.severeTempbanEnabled = config.getBoolean("punishments.severe-tempban-enabled", true);
        this.preventDuplicatePunishments = config.getBoolean("punishments.prevent-duplicate-punishments", true);

        this.warningMessage = config.getString("punishments.warning-message", warningMessage);
        this.kickMessage = config.getString("punishments.kick-message", kickMessage);
        this.tempbanMessage = config.getString("punishments.tempban-message", tempbanMessage);
        this.permbanMessage = config.getString("punishments.permban-message", permbanMessage);
    }

    public boolean isAutomaticEnabled() { return automaticEnabled; }
    public int getWarningsBeforeTempban() { return warningsBeforeTempban; }
    public String getTempbanDurationString() { return tempbanDurationString; }
    public long getTempbanDurationMillis() { return tempbanDurationMillis; }
    public String getWarningResetString() { return warningResetString; }
    public long getWarningResetMillis() { return warningResetMillis; }
    public boolean isSaveEvidenceBeforePunishment() { return saveEvidenceBeforePunishment; }
    public String getRepeatedDetectionWindowString() { return repeatedDetectionWindowString; }
    public long getRepeatedDetectionWindowMillis() { return repeatedDetectionWindowMillis; }
    public boolean isRequireConfidenceThreshold() { return requireConfidenceThreshold; }
    public boolean isSevereKickEnabled() { return severeKickEnabled; }
    public boolean isSevereTempbanEnabled() { return severeTempbanEnabled; }
    public boolean isPreventDuplicatePunishments() { return preventDuplicatePunishments; }
    public String getWarningMessage() { return warningMessage; }
    public String getKickMessage() { return kickMessage; }
    public String getTempbanMessage() { return tempbanMessage; }
    public String getPermbanMessage() { return permbanMessage; }
}
