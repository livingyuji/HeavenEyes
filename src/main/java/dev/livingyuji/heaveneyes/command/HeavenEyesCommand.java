package dev.livingyuji.heaveneyes.command;

import dev.livingyuji.heaveneyes.HeavenEyesPlugin;
import dev.livingyuji.heaveneyes.check.AbstractCheck;
import dev.livingyuji.heaveneyes.model.BanRecord;
import dev.livingyuji.heaveneyes.model.CheckType;
import dev.livingyuji.heaveneyes.model.MiningProfile;
import dev.livingyuji.heaveneyes.model.PlayerProfile;
import dev.livingyuji.heaveneyes.model.WarningRecord;
import dev.livingyuji.heaveneyes.storage.CleanupManager;
import dev.livingyuji.heaveneyes.util.DurationParser;
import dev.livingyuji.heaveneyes.util.PermissionValidator;
import dev.livingyuji.heaveneyes.util.TextUtil;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main command executor and tab completer for /he, /ac, /anticheat.
 */
public class HeavenEyesCommand implements CommandExecutor, TabCompleter {

    private final HeavenEyesPlugin plugin;

    private static final List<String> COMMON_PERMS = List.of(
            "heaveneyes.help",
            "heaveneyes.version",
            "heaveneyes.report",
            "heaveneyes.status",
            "heaveneyes.alerts",
            "heaveneyes.inspect",
            "heaveneyes.violations",
            "heaveneyes.evidence",
            "heaveneyes.mining",
            "heaveneyes.ban",
            "heaveneyes.unban",
            "heaveneyes.kick",
            "heaveneyes.checks"
    );

    public HeavenEyesCommand(HeavenEyesPlugin plugin) {
        this.plugin = plugin;
    }

    private void sendMessage(CommandSender sender, String msg) {
        sender.sendMessage(LegacyComponentSerializer.legacySection().deserialize(TextUtil.colorize(msg)));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            handleHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "version" -> handleVersion(sender);
            case "status" -> handleStatus(sender);
            case "report" -> handleReport(sender, args);
            case "reload" -> handleReload(sender);
            case "checks" -> handleChecksList(sender);
            case "check" -> handleCheckToggle(sender, args);
            case "alerts" -> handleAlerts(sender, args);
            case "inspect" -> handleInspect(sender, args);
            case "violations" -> handleViolations(sender, args);
            case "evidence" -> handleEvidence(sender, args);
            case "history" -> handleHistory(sender, args);
            case "mining" -> handleMining(sender, args);
            case "baninfo" -> handleBanInfo(sender, args);
            case "punishments" -> handlePunishments(sender, args);
            case "kick" -> handleKick(sender, args);
            case "ban" -> handleBan(sender, args);
            case "tempban" -> handleTempban(sender, args);
            case "unban" -> handleUnban(sender, args);
            case "pardon" -> handlePardon(sender, args);
            case "exempt" -> handleExempt(sender, args);
            case "unexempt" -> handleUnexempt(sender, args);
            case "staff" -> handleStaff(sender, args);
            case "perm" -> handlePerm(sender, args);
            case "backup" -> handleBackup(sender);
            case "export" -> handleExport(sender, args);
            case "cleanup" -> handleCleanup(sender, args);
            default -> sendMessage(sender, plugin.getMessagesConfig().getUnknownCommand());
        }

        return true;
    }

    private void handleHelp(CommandSender sender) {
        if (!sender.hasPermission("heaveneyes.help")) {
            sendMessage(sender, plugin.getMessagesConfig().getNoPermission());
            return;
        }

        sendMessage(sender, "&8&m----------------&r &cHeaven Eyes Commands &8&m----------------");
        sendMessage(sender, "&e/he help &8- &7View available commands");
        sendMessage(sender, "&e/he version &8- &7Display plugin version information");
        sendMessage(sender, "&e/he status &8- &7View anti-cheat system status");
        sendMessage(sender, "&e/he report <player> <reason> &8- &7Submit player report");

        if (sender.hasPermission("heaveneyes.inspect")) {
            sendMessage(sender, "&e/he inspect <player> &8- &7Inspect real-time player data");
            sendMessage(sender, "&e/he violations <player> &8- &7View violation levels");
            sendMessage(sender, "&e/he history <player> &8- &7View player history and warnings");
            sendMessage(sender, "&e/he mining <player> &8- &7Inspect mining analytics & ratios");
        }

        if (sender.hasPermission("heaveneyes.evidence")) {
            sendMessage(sender, "&e/he evidence <player> &8- &7List recorded incidents");
        }

        if (sender.hasPermission("heaveneyes.alerts")) {
            sendMessage(sender, "&e/he alerts [on|off] &8- &7Toggle staff violation alerts");
        }

        if (sender.hasPermission("heaveneyes.checks")) {
            sendMessage(sender, "&e/he checks &8- &7List all detection modules");
            sendMessage(sender, "&e/he check <name> <on|off> &8- &7Toggle individual checks");
        }

        if (sender.hasPermission("heaveneyes.kick")) {
            sendMessage(sender, "&e/he kick <player> <reason> &8- &7Kick player with anti-cheat reason");
        }

        if (sender.hasPermission("heaveneyes.ban")) {
            sendMessage(sender, "&e/he ban <player> <reason> &8- &7Permanently ban player");
            sendMessage(sender, "&e/he tempban <player> <duration> <reason> &8- &7Temporarily ban player");
        }

        if (sender.hasPermission("heaveneyes.unban")) {
            sendMessage(sender, "&e/he unban <player> <reason> &8- &7Unban player by name");
            sendMessage(sender, "&e/he pardon <ban-id> <reason> &8- &7Pardon ban by Ban ID");
        }

        if (sender.hasPermission("heaveneyes.exempt")) {
            sendMessage(sender, "&e/he exempt <player> &8- &7Exempt player from checks");
            sendMessage(sender, "&e/he unexempt <player> &8- &7Remove exemption from player");
        }

        if (sender.hasPermission("heaveneyes.staff")) {
            sendMessage(sender, "&e/he staff <add|remove|list> &8- &7Manage staff alert members");
        }

        if (sender.hasPermission("heaveneyes.permissions")) {
            sendMessage(sender, "&e/he perm <add|remove|list|check> &8- &7Manage LuckPerms permissions");
        }

        if (sender.hasPermission("heaveneyes.backup")) {
            sendMessage(sender, "&e/he backup &8- &7Generate compressed archive backup");
            sendMessage(sender, "&e/he export <player> &8- &7Export full audit file for player");
            sendMessage(sender, "&e/he cleanup <preview|run> &8- &7Evaluate or execute data cleanup");
        }

        if (sender.hasPermission("heaveneyes.reload")) {
            sendMessage(sender, "&e/he reload &8- &7Reload anti-cheat configuration files");
        }

        sendMessage(sender, "&8&m----------------------------------------------------");
    }

    private void handleVersion(CommandSender sender) {
        if (!sender.hasPermission("heaveneyes.version")) {
            sendMessage(sender, plugin.getMessagesConfig().getNoPermission());
            return;
        }
        Map<String, String> placeholders = Map.of(
                "version", plugin.getDescription().getVersion(),
                "author", plugin.getDescription().getAuthors().isEmpty() ? "LivingYuji" : plugin.getDescription().getAuthors().get(0)
        );
        sendMessage(sender, TextUtil.format(plugin.getMessagesConfig().getVersion(), placeholders));
    }

    private void handleStatus(CommandSender sender) {
        if (!plugin.getPluginConfig().isPublicStatus() && !sender.hasPermission("heaveneyes.status")) {
            sendMessage(sender, plugin.getMessagesConfig().getNoPermission());
            return;
        }

        sendMessage(sender, "&8&m----------------&r &cHeaven Eyes Status &8&m----------------");
        sendMessage(sender, "&7Platform: &fPaper 1.21.x &8| &7Java: &f" + System.getProperty("java.version"));
        sendMessage(sender, "&7Status: &aActive &8| &7Active Checks: &a" + plugin.getCheckManager().getEnabledCount() + "&7/&f" + plugin.getCheckManager().getAllChecks().size());
        sendMessage(sender, "&7Active Bans: &c" + plugin.getBanManager().getActiveBansCount() + " &8| &7Cached Incidents: &e" + plugin.getEvidenceManager().getCachedIncidentsCount());
        sendMessage(sender, "&7LuckPerms Integration: " + (plugin.getLuckPermsIntegration().isAvailable() ? "&aConnected" : "&7Unavailable (Soft-Depend)"));
        sendMessage(sender, "&8&m----------------------------------------------------");
    }

    private void handleReport(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heaveneyes.report")) {
            sendMessage(sender, plugin.getMessagesConfig().getNoPermission());
            return;
        }
        if (args.length < 3) {
            sendMessage(sender, "&cUsage: /he report <player> <reason>");
            return;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sendMessage(sender, "&cPlayer &e" + targetName + " &cmust be online to submit a report.");
            return;
        }

        String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        plugin.getAlertManager().sendReportAlert(sender.getName(), target.getName(), reason);

        Map<String, String> placeholders = Map.of("target", target.getName(), "reason", reason);
        sendMessage(sender, TextUtil.format(plugin.getMessagesConfig().getReportSent(), placeholders));
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("heaveneyes.reload")) {
            sendMessage(sender, plugin.getMessagesConfig().getNoPermission());
            return;
        }

        long start = System.currentTimeMillis();
        plugin.reloadAll();
        long diff = System.currentTimeMillis() - start;

        Map<String, String> placeholders = Map.of("time", String.valueOf(diff));
        sendMessage(sender, TextUtil.format(plugin.getMessagesConfig().getReloadSuccess(), placeholders));
    }

    private void handleChecksList(CommandSender sender) {
        if (!sender.hasPermission("heaveneyes.checks")) {
            sendMessage(sender, plugin.getMessagesConfig().getNoPermission());
            return;
        }

        sendMessage(sender, "&8&m----------------&r &cHeaven Eyes Checks &8&m----------------");
        for (AbstractCheck c : plugin.getCheckManager().getAllChecks()) {
            String status = c.isEnabled() ? "&a[ENABLED]" : "&c[DISABLED]";
            sendMessage(sender, String.format("&e%s &8(%s) %s &7- Action: &f%s&7, MinConf: &b%.2f",
                    c.getName(), c.getCategory().getDisplayName(), status, c.getAction(), c.getMinimumConfidence()));
        }
        sendMessage(sender, "&8&m----------------------------------------------------");
    }

    private void handleCheckToggle(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heaveneyes.checks")) {
            sendMessage(sender, plugin.getMessagesConfig().getNoPermission());
            return;
        }
        if (args.length < 3) {
            sendMessage(sender, "&cUsage: /he check <check> <on|off>");
            return;
        }

        AbstractCheck check = plugin.getCheckManager().getCheck(args[1]);
        if (check == null) {
            sendMessage(sender, "&cCheck &e" + args[1] + " &cwas not found. Type &e/he checks &cfor a list.");
            return;
        }

        String stateStr = args[2].toLowerCase(Locale.ROOT);
        boolean enable = stateStr.equals("on") || stateStr.equals("true") || stateStr.equals("enable");
        check.setEnabled(enable);

        String state = enable ? "&aenabled" : "&cdisabled";
        sendMessage(sender, "&7Check &e" + check.getName() + " &7is now " + state + "&7.");
        plugin.getAuditLogger().logAction(String.format("CHECK_TOGGLED check=%s enabled=%b actor=%s", check.getName(), enable, sender.getName()));
    }

    private void handleAlerts(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heaveneyes.alerts")) {
            sendMessage(sender, plugin.getMessagesConfig().getNoPermission());
            return;
        }
        if (!(sender instanceof Player player)) {
            sendMessage(sender, "&cThis command can only be used by players.");
            return;
        }

        UUID uuid = player.getUniqueId();
        boolean newState;
        if (args.length >= 2) {
            newState = args[1].equalsIgnoreCase("on");
        } else {
            newState = !plugin.getAlertManager().isAlertsEnabledFor(uuid);
        }

        plugin.getAlertManager().setAlertsEnabled(uuid, newState);
        String state = newState ? "&aenabled" : "&cdisabled";
        sendMessage(sender, "&7Staff violation alerts are now " + state + "&7.");
    }

    private void handleInspect(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heaveneyes.inspect")) {
            sendMessage(sender, plugin.getMessagesConfig().getNoPermission());
            return;
        }
        if (args.length < 2) {
            sendMessage(sender, "&cUsage: /he inspect <player>");
            return;
        }

        UUID targetUuid = plugin.getPlayerDataManager().getUuidByName(args[1]);
        if (targetUuid == null) {
            sendMessage(sender, "&cPlayer &e" + args[1] + " &cwas not found.");
            return;
        }

        Player onlineTarget = Bukkit.getPlayer(targetUuid);
        PlayerProfile profile = plugin.getPlayerDataManager().getPlayerProfile(targetUuid, args[1]);

        sendMessage(sender, "&8&m----------------&r &cHeaven Eyes Inspection &8&m----------------");
        sendMessage(sender, "&7Player: &e" + (onlineTarget != null ? onlineTarget.getName() : args[1]) + " &8(UUID: &7" + targetUuid + "&8)");
        sendMessage(sender, "&7Status: " + (onlineTarget != null ? "&aOnline &8(Ping: " + onlineTarget.getPing() + "ms)" : "&7Offline"));
        sendMessage(sender, "&7Exempt: " + (plugin.getPunishmentEngine().isExempt(targetUuid) ? "&aYes" : "&cNo"));
        sendMessage(sender, "&7Total Violations: &e" + (profile != null ? profile.getTotalViolations() : 0));
        sendMessage(sender, "&7Active Warnings: &c" + plugin.getWarningManager().getActiveWarningCount(targetUuid));
        sendMessage(sender, "&7Total Bans: &c" + (profile != null ? profile.getTotalBans() : 0));
        sendMessage(sender, "&8&m----------------------------------------------------");
    }

    private void handleViolations(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heaveneyes.violations")) {
            sendMessage(sender, plugin.getMessagesConfig().getNoPermission());
            return;
        }
        if (args.length < 2) {
            sendMessage(sender, "&cUsage: /he violations <player>");
            return;
        }

        UUID targetUuid = plugin.getPlayerDataManager().getUuidByName(args[1]);
        if (targetUuid == null) {
            sendMessage(sender, "&cPlayer &e" + args[1] + " &cwas not found.");
            return;
        }

        sendMessage(sender, "&8&m----------------&r &cViolations for &e" + args[1] + " &8&m----------------");
        boolean hasVl = false;
        for (AbstractCheck c : plugin.getCheckManager().getAllChecks()) {
            int vl = c.getViolationLevel(targetUuid);
            if (vl > 0) {
                hasVl = true;
                sendMessage(sender, String.format("&7- &e%s&7: VL &c%d", c.getName(), vl));
            }
        }
        if (!hasVl) {
            sendMessage(sender, "&aNo active violation levels recorded.");
        }
        sendMessage(sender, "&8&m----------------------------------------------------");
    }

    private void handleEvidence(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heaveneyes.evidence")) {
            sendMessage(sender, plugin.getMessagesConfig().getNoPermission());
            return;
        }
        if (args.length < 2) {
            sendMessage(sender, "&cUsage: /he evidence <player>");
            return;
        }

        UUID targetUuid = plugin.getPlayerDataManager().getUuidByName(args[1]);
        if (targetUuid == null) {
            sendMessage(sender, "&cPlayer &e" + args[1] + " &cwas not found.");
            return;
        }

        List<String> list = plugin.getPlayerDataManager().getPlayerEvidenceList(targetUuid);
        sendMessage(sender, "&8&m----------------&r &cEvidence Files for &e" + args[1] + " &8&m----------------");
        if (list.isEmpty()) {
            sendMessage(sender, "&aNo recorded incidents found for this player.");
        } else {
            for (String item : list) {
                sendMessage(sender, "&7- &f" + item);
            }
        }
        sendMessage(sender, "&8&m----------------------------------------------------");
    }

    private void handleHistory(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heaveneyes.history")) {
            sendMessage(sender, plugin.getMessagesConfig().getNoPermission());
            return;
        }
        if (args.length < 2) {
            sendMessage(sender, "&cUsage: /he history <player>");
            return;
        }

        UUID targetUuid = plugin.getPlayerDataManager().getUuidByName(args[1]);
        if (targetUuid == null) {
            sendMessage(sender, "&cPlayer &e" + args[1] + " &cwas not found.");
            return;
        }

        List<WarningRecord> warnings = plugin.getWarningManager().getActiveWarnings(targetUuid);
        List<BanRecord> bans = plugin.getBanManager().getPlayerBanHistory(targetUuid);

        sendMessage(sender, "&8&m----------------&r &cHistory for &e" + args[1] + " &8&m----------------");
        sendMessage(sender, "&eActive Warnings (" + warnings.size() + "):");
        for (WarningRecord w : warnings) {
            sendMessage(sender, String.format("&7- Warning #%d: &c%s &8(&7Evidence: &e%s&8)",
                    w.getCount(), w.getCheckType().getIdentifier(), w.getEvidenceId()));
        }

        sendMessage(sender, "&eBans Record (" + bans.size() + "):");
        for (BanRecord b : bans) {
            String status = b.isActive() ? "&c[ACTIVE]" : "&a[PARDONED/EXPIRED]";
            sendMessage(sender, String.format("&7- %s &e%s &7(%s) - &f'%s'", status, b.getBanId(), b.getType(), b.getReason()));
        }
        sendMessage(sender, "&8&m----------------------------------------------------");
    }

    private void handleMining(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heaveneyes.mining")) {
            sendMessage(sender, plugin.getMessagesConfig().getNoPermission());
            return;
        }
        if (args.length < 2) {
            sendMessage(sender, "&cUsage: /he mining <player>");
            return;
        }

        UUID targetUuid = plugin.getPlayerDataManager().getUuidByName(args[1]);
        if (targetUuid == null) {
            sendMessage(sender, "&cPlayer &e" + args[1] + " &cwas not found.");
            return;
        }

        MiningProfile mining = plugin.getPlayerDataManager().getMiningProfile(targetUuid);
        sendMessage(sender, "&8&m----------------&r &cMining Analytics for &e" + args[1] + " &8&m----------------");
        if (mining == null) {
            sendMessage(sender, "&7No mining data recorded.");
        } else {
            long totalBase = mining.getTotalBaseBlocks();
            sendMessage(sender, String.format("&7Base Blocks Broken: &f%d &8(Stone: %d, Deepslate: %d)",
                    totalBase, mining.getStoneMined(), mining.getDeepslateMined()));
            sendMessage(sender, String.format("&7Diamonds Mined: &b%d &8(Ratio: &e%.2f%%&8)",
                    mining.getDiamondOreMined(), mining.getDiamondRatio() * 100.0));
            sendMessage(sender, String.format("&7Ancient Debris Mined: &d%d &8(Ratio: &e%.2f%%&8)",
                    mining.getAncientDebrisMined(), mining.getAncientDebrisRatio() * 100.0));
            sendMessage(sender, String.format("&7Gold: &6%d &8| &7Iron: &f%d &8| &7Emerald: &a%d",
                    mining.getGoldOreMined(), mining.getIronOreMined(), mining.getEmeraldOreMined()));
            sendMessage(sender, "&7Suspicious Direct Veins: &c" + mining.getSuspiciousVeinDiscoveries());
        }
        sendMessage(sender, "&8&m----------------------------------------------------");
    }

    private void handleBanInfo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heaveneyes.baninfo")) {
            sendMessage(sender, plugin.getMessagesConfig().getNoPermission());
            return;
        }
        if (args.length < 2) {
            sendMessage(sender, "&cUsage: /he baninfo <player|ban-id>");
            return;
        }

        String input = args[1];
        BanRecord ban = plugin.getBanManager().getBanById(input);
        if (ban == null) {
            UUID targetUuid = plugin.getPlayerDataManager().getUuidByName(input);
            if (targetUuid != null) {
                ban = plugin.getBanManager().getActiveBan(targetUuid);
            }
        }

        if (ban == null) {
            sendMessage(sender, "&cNo active or historical ban found for &e" + input + "&c.");
            return;
        }

        sendMessage(sender, "&8&m----------------&r &cBan Information: &e" + ban.getBanId() + " &8&m----------------");
        sendMessage(sender, "&7Target Player: &e" + ban.getPlayerName() + " &8(" + ban.getPlayerUuid() + "&8)");
        sendMessage(sender, "&7Type: &f" + ban.getType() + " &8| &7Source: &f" + ban.getSource());
        sendMessage(sender, "&7Reason: &f" + ban.getReason());
        sendMessage(sender, "&7Actor: &f" + ban.getActor());
        sendMessage(sender, "&7Active: " + (ban.isActive() ? "&cYes" : "&aNo (Pardoned/Expired)"));
        if (ban.getExpiresAt() > 0) {
            sendMessage(sender, "&7Remaining: &e" + DurationParser.format(ban.getRemainingMillis()));
        }
        if (!ban.isActive() && ban.getUnbannedBy() != null) {
            sendMessage(sender, "&7Pardoned By: &f" + ban.getUnbannedBy() + " &8(Reason: " + ban.getUnbanReason() + ")");
        }
        sendMessage(sender, "&8&m----------------------------------------------------");
    }

    private void handlePunishments(CommandSender sender, String[] args) {
        handleHistory(sender, args);
    }

    private void handleKick(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heaveneyes.kick")) {
            sendMessage(sender, plugin.getMessagesConfig().getNoPermission());
            return;
        }
        if (args.length < 3) {
            sendMessage(sender, "&cUsage: /he kick <player> <reason>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendMessage(sender, "&cPlayer &e" + args[1] + " &cis not online.");
            return;
        }

        String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        plugin.getPunishmentEngine().executeKick(target, reason, "MANUAL-KICK");
        sendMessage(sender, "&aPlayer &e" + target.getName() + " &awas kicked for &f'" + reason + "'&a.");
    }

    private void handleBan(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heaveneyes.ban")) {
            sendMessage(sender, plugin.getMessagesConfig().getNoPermission());
            return;
        }
        if (args.length < 3) {
            sendMessage(sender, "&cUsage: /he ban <player> <reason>");
            return;
        }

        String playerName = args[1];
        UUID targetUuid = plugin.getPlayerDataManager().getUuidByName(playerName);
        if (targetUuid == null) {
            sendMessage(sender, "&cPlayer &e" + playerName + " &cwas not found.");
            return;
        }

        String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        BanRecord ban = plugin.getBanManager().createBan(
                targetUuid,
                playerName,
                BanRecord.Type.PERMANENT,
                reason,
                BanRecord.Source.MANUAL,
                null,
                0,
                sender.getName(),
                null
        );

        sendMessage(sender, "&aPlayer &e" + playerName + " &awas permanently banned with Ban ID &e" + ban.getBanId() + "&a.");
    }

    private void handleTempban(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heaveneyes.ban")) {
            sendMessage(sender, plugin.getMessagesConfig().getNoPermission());
            return;
        }
        if (args.length < 4) {
            sendMessage(sender, "&cUsage: /he tempban <player> <duration> <reason>");
            return;
        }

        String playerName = args[1];
        UUID targetUuid = plugin.getPlayerDataManager().getUuidByName(playerName);
        if (targetUuid == null) {
            sendMessage(sender, "&cPlayer &e" + playerName + " &cwas not found.");
            return;
        }

        long durationMillis;
        try {
            durationMillis = DurationParser.parseMillis(args[2]);
        } catch (IllegalArgumentException e) {
            sendMessage(sender, "&cInvalid duration format: &e" + args[2] + "&c. Example: 30m, 1h, 7d.");
            return;
        }

        String reason = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        BanRecord ban = plugin.getBanManager().createBan(
                targetUuid,
                playerName,
                BanRecord.Type.TEMPORARY,
                reason,
                BanRecord.Source.MANUAL,
                null,
                durationMillis,
                sender.getName(),
                null
        );

        sendMessage(sender, "&aPlayer &e" + playerName + " &awas temporarily banned for &e" + DurationParser.format(durationMillis) + " &awith Ban ID &e" + ban.getBanId() + "&a.");
    }

    private void handleUnban(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heaveneyes.unban")) {
            sendMessage(sender, plugin.getMessagesConfig().getNoPermission());
            return;
        }
        if (args.length < 3) {
            sendMessage(sender, "&cUsage: /he unban <player> <reason>");
            return;
        }

        String playerName = args[1];
        UUID targetUuid = plugin.getPlayerDataManager().getUuidByName(playerName);
        if (targetUuid == null) {
            sendMessage(sender, "&cPlayer &e" + playerName + " &cwas not found.");
            return;
        }

        String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        boolean unbanned = plugin.getBanManager().unban(targetUuid, sender.getName(), reason);
        if (unbanned) {
            sendMessage(sender, "&aPlayer &e" + playerName + " &awas successfully unbanned.");
        } else {
            sendMessage(sender, "&cPlayer &e" + playerName + " &cdoes not have an active ban.");
        }
    }

    private void handlePardon(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heaveneyes.unban")) {
            sendMessage(sender, plugin.getMessagesConfig().getNoPermission());
            return;
        }
        if (args.length < 3) {
            sendMessage(sender, "&cUsage: /he pardon <ban-id> <reason>");
            return;
        }

        String banId = args[1];
        String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        BanRecord ban = plugin.getBanManager().unbanById(banId, sender.getName(), reason);

        if (ban != null) {
            sendMessage(sender, "&aBan &e" + banId + " &afor &e" + ban.getPlayerName() + " &awas pardoned successfully.");
        } else {
            sendMessage(sender, "&cNo active ban found with ID &e" + banId + "&c.");
        }
    }

    private void handleExempt(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heaveneyes.exempt")) {
            sendMessage(sender, plugin.getMessagesConfig().getNoPermission());
            return;
        }
        if (args.length < 2) {
            sendMessage(sender, "&cUsage: /he exempt <player>");
            return;
        }

        UUID targetUuid = plugin.getPlayerDataManager().getUuidByName(args[1]);
        if (targetUuid == null) {
            sendMessage(sender, "&cPlayer &e" + args[1] + " &cwas not found.");
            return;
        }

        plugin.getPunishmentEngine().setExempt(targetUuid, true);
        sendMessage(sender, "&aPlayer &e" + args[1] + " &ais now exempt from anti-cheat checks.");
        plugin.getAuditLogger().logAction(String.format("EXEMPT_ADDED player=%s(%s) actor=%s", args[1], targetUuid, sender.getName()));
    }

    private void handleUnexempt(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heaveneyes.exempt")) {
            sendMessage(sender, plugin.getMessagesConfig().getNoPermission());
            return;
        }
        if (args.length < 2) {
            sendMessage(sender, "&cUsage: /he unexempt <player>");
            return;
        }

        UUID targetUuid = plugin.getPlayerDataManager().getUuidByName(args[1]);
        if (targetUuid == null) {
            sendMessage(sender, "&cPlayer &e" + args[1] + " &cwas not found.");
            return;
        }

        plugin.getPunishmentEngine().setExempt(targetUuid, false);
        sendMessage(sender, "&cPlayer &e" + args[1] + " &cis no longer exempt from anti-cheat checks.");
        plugin.getAuditLogger().logAction(String.format("EXEMPT_REMOVED player=%s(%s) actor=%s", args[1], targetUuid, sender.getName()));
    }

    private void handleStaff(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heaveneyes.staff")) {
            sendMessage(sender, plugin.getMessagesConfig().getNoPermission());
            return;
        }
        if (args.length < 2) {
            sendMessage(sender, "&cUsage: /he staff <add|remove|list> [player]");
            return;
        }

        String action = args[1].toLowerCase(Locale.ROOT);
        if (action.equals("list")) {
            sendMessage(sender, "&8&m--------&r &cHeaven Eyes Staff Members &8&m--------");
            var members = plugin.getStaffConfig().getStaffMembers();
            if (members.isEmpty()) {
                sendMessage(sender, "&7No staff members recorded in staff.yml.");
            } else {
                for (Map.Entry<UUID, String> entry : members.entrySet()) {
                    boolean alerts = plugin.getAlertManager().isAlertsEnabledFor(entry.getKey());
                    sendMessage(sender, String.format("&7- &e%s &8(Alerts: %s&8)", entry.getValue(), alerts ? "&aON" : "&cOFF"));
                }
            }
            sendMessage(sender, "&8&m------------------------------------------------");
            return;
        }

        if (args.length < 3) {
            sendMessage(sender, "&cUsage: /he staff " + action + " <player>");
            return;
        }

        String targetName = args[2];
        UUID targetUuid = plugin.getPlayerDataManager().getUuidByName(targetName);
        if (targetUuid == null) {
            sendMessage(sender, "&cPlayer &e" + targetName + " &cwas not found.");
            return;
        }

        if (action.equals("add")) {
            boolean added = plugin.getStaffConfig().addStaff(targetUuid, targetName);
            if (added) {
                sendMessage(sender, "&aAdded &e" + targetName + " &ato Heaven Eyes staff alerts list.");
                plugin.getAuditLogger().logAction(String.format("STAFF_ADDED player=%s(%s) actor=%s", targetName, targetUuid, sender.getName()));
            } else {
                sendMessage(sender, "&e" + targetName + " &cis already on the staff list.");
            }
        } else if (action.equals("remove")) {
            boolean removed = plugin.getStaffConfig().removeStaff(targetUuid);
            if (removed) {
                sendMessage(sender, "&cRemoved &e" + targetName + " &cfrom Heaven Eyes staff alerts list.");
                plugin.getAuditLogger().logAction(String.format("STAFF_REMOVED player=%s(%s) actor=%s", targetName, targetUuid, sender.getName()));
            } else {
                sendMessage(sender, "&e" + targetName + " &cis not on the staff list.");
            }
        }
    }

    private void handlePerm(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heaveneyes.permissions")) {
            sendMessage(sender, plugin.getMessagesConfig().getNoPermission());
            return;
        }

        if (!plugin.getLuckPermsIntegration().isAvailable()) {
            sendMessage(sender, "&cLuckPerms is not installed or enabled. Permission commands require LuckPerms.");
            return;
        }

        if (args.length < 3) {
            sendMessage(sender, "&cUsage: /he perm <add|remove|list|check> <player> [permission]");
            return;
        }

        String op = args[1].toLowerCase(Locale.ROOT);
        String targetName = args[2];
        UUID targetUuid = plugin.getPlayerDataManager().getUuidByName(targetName);
        if (targetUuid == null) {
            sendMessage(sender, "&cPlayer &e" + targetName + " &cwas not found.");
            return;
        }

        if (op.equals("list")) {
            plugin.getLuckPermsIntegration().listPermissions(targetUuid).thenAccept(perms -> {
                sendMessage(sender, "&8&m--------&r &cPermissions for &e" + targetName + " &8&m--------");
                if (perms.isEmpty()) {
                    sendMessage(sender, "&7No explicit user permissions set.");
                } else {
                    for (String p : perms) {
                        sendMessage(sender, "&7- &f" + p);
                    }
                }
                sendMessage(sender, "&8&m------------------------------------------------");
            });
            return;
        }

        if (args.length < 4) {
            sendMessage(sender, "&cUsage: /he perm " + op + " " + targetName + " <permission>");
            return;
        }

        String node = args[3];

        if (op.equals("check")) {
            plugin.getLuckPermsIntegration().hasPermission(targetUuid, node).thenAccept(has -> {
                sendMessage(sender, "&7Player &e" + targetName + " &7has permission &e" + node + "&7: " + (has ? "&aYES" : "&cNO"));
            });
            return;
        }

        // Security validation
        if (!PermissionValidator.isValidNode(node)) {
            sendMessage(sender, "&cInvalid permission node format &e'" + node + "'&c.");
            return;
        }

        if (PermissionValidator.isDangerousNode(node)) {
            sendMessage(sender, "&cCannot modify root or dangerous wildcard permissions via Heaven Eyes.");
            return;
        }

        // Prevent self-granting administrative permissions
        if (sender instanceof Player p && p.getUniqueId().equals(targetUuid) && node.toLowerCase(Locale.ROOT).contains("admin")) {
            sendMessage(sender, "&cYou cannot grant yourself administrative permissions.");
            return;
        }

        if (op.equals("add")) {
            plugin.getLuckPermsIntegration().addPermission(targetUuid, node).thenAccept(success -> {
                if (success) {
                    sendMessage(sender, "&aSuccessfully granted &e" + node + " &ato &f" + targetName + "&a.");
                    plugin.getAuditLogger().logAction(String.format("PERM_ADDED player=%s(%s) node=%s actor=%s", targetName, targetUuid, node, sender.getName()));
                } else {
                    sendMessage(sender, "&cFailed to assign permission. Player may already possess it.");
                }
            });
        } else if (op.equals("remove")) {
            plugin.getLuckPermsIntegration().removePermission(targetUuid, node).thenAccept(success -> {
                if (success) {
                    sendMessage(sender, "&aSuccessfully revoked &e" + node + " &afrom &f" + targetName + "&a.");
                    plugin.getAuditLogger().logAction(String.format("PERM_REMOVED player=%s(%s) node=%s actor=%s", targetName, targetUuid, node, sender.getName()));
                } else {
                    sendMessage(sender, "&cFailed to revoke permission. Player may not have had it assigned.");
                }
            });
        }
    }

    private void handleBackup(CommandSender sender) {
        if (!sender.hasPermission("heaveneyes.backup")) {
            sendMessage(sender, plugin.getMessagesConfig().getNoPermission());
            return;
        }

        sendMessage(sender, "&7Creating compressed Heaven Eyes backup in background...");
        plugin.getBackupManager().createBackupAsync().thenAccept(file -> {
            sendMessage(sender, "&aBackup created successfully: &e" + file.getName() + " &8(" + (file.length() / 1024) + " KB)");
        }).exceptionally(ex -> {
            sendMessage(sender, "&cFailed to create backup: " + ex.getMessage());
            return null;
        });
    }

    private void handleExport(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heaveneyes.export")) {
            sendMessage(sender, plugin.getMessagesConfig().getNoPermission());
            return;
        }
        if (args.length < 2) {
            sendMessage(sender, "&cUsage: /he export <player>");
            return;
        }

        String targetName = args[1];
        UUID targetUuid = plugin.getPlayerDataManager().getUuidByName(targetName);
        if (targetUuid == null) {
            sendMessage(sender, "&cPlayer &e" + targetName + " &cwas not found.");
            return;
        }

        sendMessage(sender, "&7Exporting comprehensive audit report for &e" + targetName + "&7...");
        plugin.getExportManager().exportPlayerReportAsync(targetUuid, targetName).thenAccept(file -> {
            sendMessage(sender, "&aReport exported successfully: &e" + file.getName());
        }).exceptionally(ex -> {
            sendMessage(sender, "&cExport failed: " + ex.getMessage());
            return null;
        });
    }

    private void handleCleanup(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heaveneyes.cleanup")) {
            sendMessage(sender, plugin.getMessagesConfig().getNoPermission());
            return;
        }
        if (args.length < 2) {
            sendMessage(sender, "&cUsage: /he cleanup <preview|run>");
            return;
        }

        String mode = args[1].toLowerCase(Locale.ROOT);
        if (mode.equals("preview")) {
            CleanupManager.CleanupReport report = plugin.getCleanupManager().evaluateCleanup(false);
            sendMessage(sender, "&8&m----------------&r &cData Cleanup Preview &8&m----------------");
            sendMessage(sender, "&7Eligible Incident Files: &e" + report.incidentsCount + " &8(" + (report.incidentsBytes / 1024) + " KB)");
            sendMessage(sender, "&7Eligible Old Violation Files: &e" + report.playerRecordsCount + " &8(" + (report.playerRecordsBytes / 1024) + " KB)");
            sendMessage(sender, "&7Total Files to be Removed: &c" + report.getTotalFiles() + " &8(" + (report.getTotalBytes() / 1024) + " KB)");
            sendMessage(sender, "&7To execute cleanup, run: &e/he cleanup run");
            sendMessage(sender, "&8&m----------------------------------------------------");
        } else if (mode.equals("run")) {
            CleanupManager.CleanupReport report = plugin.getCleanupManager().evaluateCleanup(true);
            sendMessage(sender, "&aCleanup completed: &e" + report.getTotalFiles() + " &afiles removed, reclaiming &e" + (report.getTotalBytes() / 1024) + " KB&a.");
        } else {
            sendMessage(sender, "&cUsage: /he cleanup <preview|run>");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First level commands filtered by permission
            List<String> subcommands = new ArrayList<>();
            subcommands.add("help");
            subcommands.add("version");
            subcommands.add("status");
            subcommands.add("report");

            if (sender.hasPermission("heaveneyes.inspect")) {
                subcommands.add("inspect");
                subcommands.add("violations");
                subcommands.add("history");
                subcommands.add("mining");
                subcommands.add("punishments");
            }
            if (sender.hasPermission("heaveneyes.evidence")) subcommands.add("evidence");
            if (sender.hasPermission("heaveneyes.baninfo")) subcommands.add("baninfo");
            if (sender.hasPermission("heaveneyes.alerts")) subcommands.add("alerts");
            if (sender.hasPermission("heaveneyes.checks")) {
                subcommands.add("checks");
                subcommands.add("check");
            }
            if (sender.hasPermission("heaveneyes.kick")) subcommands.add("kick");
            if (sender.hasPermission("heaveneyes.ban")) {
                subcommands.add("ban");
                subcommands.add("tempban");
            }
            if (sender.hasPermission("heaveneyes.unban")) {
                subcommands.add("unban");
                subcommands.add("pardon");
            }
            if (sender.hasPermission("heaveneyes.exempt")) {
                subcommands.add("exempt");
                subcommands.add("unexempt");
            }
            if (sender.hasPermission("heaveneyes.staff")) subcommands.add("staff");
            if (sender.hasPermission("heaveneyes.permissions")) subcommands.add("perm");
            if (sender.hasPermission("heaveneyes.backup")) subcommands.add("backup");
            if (sender.hasPermission("heaveneyes.export")) subcommands.add("export");
            if (sender.hasPermission("heaveneyes.cleanup")) subcommands.add("cleanup");
            if (sender.hasPermission("heaveneyes.reload")) subcommands.add("reload");

            return filter(subcommands, args[0]);
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (args.length == 2) {
            switch (sub) {
                case "report", "inspect", "violations", "evidence", "history", "mining",
                     "punishments", "kick", "ban", "tempban", "exempt", "unexempt", "export" -> {
                    return filter(getOnlinePlayerNames(), args[1]);
                }
                case "unban" -> {
                    // Suggest names of currently active banned players
                    List<String> bannedNames = plugin.getBanManager().getAllActiveBans().stream()
                            .map(BanRecord::getPlayerName)
                            .collect(Collectors.toList());
                    return filter(bannedNames, args[1]);
                }
                case "baninfo" -> {
                    List<String> banTargets = new ArrayList<>(getOnlinePlayerNames());
                    for (BanRecord b : plugin.getBanManager().getAllActiveBans()) {
                        banTargets.add(b.getBanId());
                    }
                    return filter(banTargets, args[1]);
                }
                case "pardon" -> {
                    List<String> banIds = plugin.getBanManager().getAllActiveBans().stream()
                            .map(BanRecord::getBanId)
                            .collect(Collectors.toList());
                    return filter(banIds, args[1]);
                }
                case "check" -> {
                    List<String> checkNames = plugin.getCheckManager().getAllChecks().stream()
                            .map(AbstractCheck::getName)
                            .collect(Collectors.toList());
                    return filter(checkNames, args[1]);
                }
                case "alerts" -> {
                    return filter(List.of("on", "off"), args[1]);
                }
                case "staff" -> {
                    return filter(List.of("add", "remove", "list"), args[1]);
                }
                case "perm" -> {
                    return filter(List.of("add", "remove", "list", "check"), args[1]);
                }
                case "cleanup" -> {
                    return filter(List.of("preview", "run"), args[1]);
                }
            }
        }

        if (args.length == 3) {
            switch (sub) {
                case "check" -> {
                    return filter(List.of("on", "off"), args[2]);
                }
                case "tempban" -> {
                    return filter(List.of("30m", "1h", "2h", "12h", "24h", "7d", "30d"), args[2]);
                }
                case "staff" -> {
                    if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")) {
                        return filter(getOnlinePlayerNames(), args[2]);
                    }
                }
                case "perm" -> {
                    return filter(getOnlinePlayerNames(), args[2]);
                }
            }
        }

        if (args.length == 4) {
            if (sub.equals("perm")) {
                if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("check")) {
                    return filter(COMMON_PERMS, args[3]);
                }
            }
        }

        return completions;
    }

    private List<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

    private List<String> filter(Collection<String> items, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        return items.stream()
                .filter(s -> s.toLowerCase(Locale.ROOT).startsWith(lower))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
