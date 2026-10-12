package dev.livingyuji.heaveneyes.config;

import org.bukkit.configuration.file.FileConfiguration;

public class MessagesConfig {

    private String prefix = "&cHeaven Eyes &8» &7";
    private String noPermission = "&cYou do not have permission to execute this command.";
    private String playerOnly = "&cThis command can only be executed by in-game players.";
    private String unknownCommand = "&cUnknown Heaven Eyes command. Type &e/he help &cfor available commands.";
    private String playerNotFound = "&cPlayer &e{player} &cwas not found.";
    private String invalidDuration = "&cInvalid duration specified &e'{input}'&c. Format example: 30m, 1h, 24h, 7d.";
    private String invalidBanId = "&cInvalid or non-existent Ban ID: &e{id}&c.";
    private String reloadSuccess = "&aHeaven Eyes configuration and checks reloaded successfully in &e{time}ms&a.";

    private String version = "&7Running &cHeaven Eyes &7version &a{version} &7by &e{author}&7.";
    private String reportSent = "&aThank you. Your report against &e{target} &afor &f'{reason}' &ahas been submitted to staff.";
    private String reportAlert = "&8[&cHE-REPORT&8] &e{reporter} &7reported &c{target}&7: &f{reason}";
    private String staffAlert = "&8[&cHeaven Eyes&8] &e{player} &7failed &c{check} &8(VL: &e{vl}&8, Conf: &a{confidence}%&8) &7- &f{details}";
    private String staffAlertAction = "&8[&cHeaven Eyes&8] &cACTION: &e{action} &7issued to &c{player}&7. (&e{info}&7)";

    public void load(FileConfiguration config) {
        this.prefix = config.getString("messages.prefix", prefix);
        this.noPermission = config.getString("messages.no-permission", noPermission);
        this.playerOnly = config.getString("messages.player-only", playerOnly);
        this.unknownCommand = config.getString("messages.unknown-command", unknownCommand);
        this.playerNotFound = config.getString("messages.player-not-found", playerNotFound);
        this.invalidDuration = config.getString("messages.invalid-duration", invalidDuration);
        this.invalidBanId = config.getString("messages.invalid-ban-id", invalidBanId);
        this.reloadSuccess = config.getString("messages.reload-success", reloadSuccess);

        this.version = config.getString("messages.version", version);
        this.reportSent = config.getString("messages.report-sent", reportSent);
        this.reportAlert = config.getString("messages.report-alert", reportAlert);
        this.staffAlert = config.getString("messages.staff-alert", staffAlert);
        this.staffAlertAction = config.getString("messages.staff-alert-action", staffAlertAction);
    }

    public String getPrefix() { return prefix; }
    public String getNoPermission() { return noPermission; }
    public String getPlayerOnly() { return playerOnly; }
    public String getUnknownCommand() { return unknownCommand; }
    public String getPlayerNotFound() { return playerNotFound; }
    public String getInvalidDuration() { return invalidDuration; }
    public String getInvalidBanId() { return invalidBanId; }
    public String getReloadSuccess() { return reloadSuccess; }
    public String getVersion() { return version; }
    public String getReportSent() { return reportSent; }
    public String getReportAlertTemplate() { return reportAlert; }
    public String getStaffAlertTemplate() { return staffAlert; }
    public String getStaffAlertActionTemplate() { return staffAlertAction; }
}
