package dev.livingyuji.heaveneyes.config;

import dev.livingyuji.heaveneyes.util.DurationParser;
import org.bukkit.configuration.file.FileConfiguration;

public class PluginConfig {

    private boolean debug = false;
    private boolean alertsEnabled = true;
    private long alertCooldownMillis = 3000L;
    private boolean publicStatus = true;
    private int tickInterval = 20;
    private long retentionDays = 30;
    private int maxInMemoryIncidents = 500;
    private boolean exemptCreative = true;
    private boolean exemptSpectator = true;

    public void load(FileConfiguration config) {
        this.debug = config.getBoolean("settings.debug", false);
        this.alertsEnabled = config.getBoolean("settings.alerts-enabled", true);
        String cooldownStr = config.getString("settings.alert-cooldown", "3s");
        try {
            this.alertCooldownMillis = DurationParser.parseMillis(cooldownStr);
        } catch (Exception e) {
            this.alertCooldownMillis = 3000L;
        }
        this.publicStatus = config.getBoolean("settings.public-status", true);
        this.tickInterval = config.getInt("settings.tick-interval", 20);
        this.retentionDays = config.getLong("settings.retention-days", 30);
        this.maxInMemoryIncidents = config.getInt("settings.evidence.max-in-memory-incidents", 500);
        this.exemptCreative = config.getBoolean("exemptions.exempt-creative", true);
        this.exemptSpectator = config.getBoolean("exemptions.exempt-spectator", true);
    }

    public boolean isDebug() { return debug; }
    public boolean isAlertsEnabled() { return alertsEnabled; }
    public long getAlertCooldownMillis() { return alertCooldownMillis; }
    public boolean isPublicStatus() { return publicStatus; }
    public int getTickInterval() { return tickInterval; }
    public long getRetentionDays() { return retentionDays; }
    public int getMaxInMemoryIncidents() { return maxInMemoryIncidents; }
    public boolean isExemptCreative() { return exemptCreative; }
    public boolean isExemptSpectator() { return exemptSpectator; }
}
