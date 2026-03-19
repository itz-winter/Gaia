package com.gaiaac.gaia.commands;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Main command handler for /gaia.
 * Subcommands: debug, violations, reload, alerts, info
 */
public class GaiaCommand implements CommandExecutor, TabCompleter {

    private final GaiaPlugin plugin;
    private final String PREFIX = ChatColor.DARK_GRAY + "Gaia " + ChatColor.GRAY + "» " + ChatColor.RESET;

    public GaiaCommand(GaiaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("gaia.admin")) {
            sender.sendMessage(PREFIX + ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "debug":
                handleDebug(sender, args);
                break;
            case "violations":
            case "vl":
                handleViolations(sender, args);
                break;
            case "reload":
                handleReload(sender);
                break;
            case "alerts":
                handleAlerts(sender);
                break;
            case "info":
                handleInfo(sender, args);
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(PREFIX + ChatColor.GOLD + "Gaia Anticheat Commands:");
        sender.sendMessage(ChatColor.GRAY + "  /gaia debug <player>" + ChatColor.WHITE + " - Toggle debug mode for a player");
        sender.sendMessage(ChatColor.GRAY + "  /gaia violations <player>" + ChatColor.WHITE + " - View player violations");
        sender.sendMessage(ChatColor.GRAY + "  /gaia reload" + ChatColor.WHITE + " - Reload configuration");
        sender.sendMessage(ChatColor.GRAY + "  /gaia alerts" + ChatColor.WHITE + " - Toggle alert notifications");
        sender.sendMessage(ChatColor.GRAY + "  /gaia info" + ChatColor.WHITE + " - Show plugin information");
    }

    private void handleDebug(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Usage: /gaia debug <player>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Player not found: " + args[1]);
            return;
        }

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(target);
        if (data == null) {
            sender.sendMessage(PREFIX + ChatColor.RED + "No data found for " + target.getName());
            return;
        }

        sender.sendMessage(PREFIX + ChatColor.GOLD + "Debug info for " + ChatColor.WHITE + target.getName());
        sender.sendMessage(ChatColor.GRAY + "  Position: " + ChatColor.WHITE +
                String.format("%.2f, %.2f, %.2f", data.getX(), data.getY(), data.getZ()));
        sender.sendMessage(ChatColor.GRAY + "  OnGround: " + ChatColor.WHITE + data.isOnGround());
        sender.sendMessage(ChatColor.GRAY + "  Sprinting: " + ChatColor.WHITE + data.isSprinting());
        sender.sendMessage(ChatColor.GRAY + "  Sneaking: " + ChatColor.WHITE + data.isSneaking());
        sender.sendMessage(ChatColor.GRAY + "  Flying: " + ChatColor.WHITE + data.isFlying());
        sender.sendMessage(ChatColor.GRAY + "  InVehicle: " + ChatColor.WHITE + data.isInVehicle());
        sender.sendMessage(ChatColor.GRAY + "  Gliding: " + ChatColor.WHITE + data.isGliding());
        sender.sendMessage(ChatColor.GRAY + "  Swimming: " + ChatColor.WHITE + data.isSwimming());
        sender.sendMessage(ChatColor.GRAY + "  AirTicks: " + ChatColor.WHITE + data.getAirTicks());
        sender.sendMessage(ChatColor.GRAY + "  GroundTicks: " + ChatColor.WHITE + data.getGroundTicks());
        sender.sendMessage(ChatColor.GRAY + "  Ping: " + ChatColor.WHITE + data.getPing() + "ms");
        sender.sendMessage(ChatColor.GRAY + "  Client: " + ChatColor.WHITE + data.getClientBrand()
                + " (v" + data.getClientProtocolVersion() + ")");
        sender.sendMessage(ChatColor.GRAY + "  Total VL: " + ChatColor.WHITE +
                String.format("%.1f", data.getViolations().values().stream()
                        .mapToDouble(Double::doubleValue).sum()));
    }

    private void handleViolations(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Usage: /gaia violations <player>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Player not found: " + args[1]);
            return;
        }

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(target);
        if (data == null) {
            sender.sendMessage(PREFIX + ChatColor.RED + "No data found for " + target.getName());
            return;
        }

        Map<String, Double> violations = data.getViolations();
        if (violations.isEmpty() || violations.values().stream().allMatch(v -> v <= 0)) {
            sender.sendMessage(PREFIX + ChatColor.GREEN + target.getName() + " has no violations.");
            return;
        }

        sender.sendMessage(PREFIX + ChatColor.GOLD + "Violations for " + ChatColor.WHITE + target.getName());
        violations.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .forEach(entry -> {
                    ChatColor vlColor = entry.getValue() > 5 ? ChatColor.RED :
                            entry.getValue() > 2 ? ChatColor.YELLOW : ChatColor.GREEN;
                    sender.sendMessage(ChatColor.GRAY + "  " + entry.getKey() + ": "
                            + vlColor + String.format("%.1f", entry.getValue()));
                });
    }

    private void handleReload(CommandSender sender) {
        plugin.getConfigManager().reload();
        sender.sendMessage(PREFIX + ChatColor.GREEN + "Configuration reloaded successfully.");
    }

    private void handleAlerts(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(PREFIX + ChatColor.RED + "This command can only be used by players.");
            return;
        }

        Player player = (Player) sender;
        boolean toggled = plugin.getAlertManager().toggleAlerts(player);
        if (toggled) {
            sender.sendMessage(PREFIX + ChatColor.GREEN + "Alerts enabled.");
        } else {
            sender.sendMessage(PREFIX + ChatColor.RED + "Alerts disabled.");
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        sender.sendMessage(PREFIX + ChatColor.GOLD + "Gaia Anticheat");
        sender.sendMessage(ChatColor.GRAY + "  Version: " + ChatColor.WHITE +
                plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.GRAY + "  Authors: " + ChatColor.WHITE +
                String.join(", ", plugin.getDescription().getAuthors()));
        sender.sendMessage(ChatColor.GRAY + "  Checks: " + ChatColor.WHITE +
                plugin.getCheckManager().getAllChecks().size());
        sender.sendMessage(ChatColor.GRAY + "  Players tracked: " + ChatColor.WHITE +
                plugin.getPlayerDataManager().getAllPlayerData().size());
        sender.sendMessage(ChatColor.GRAY + "  QoLPlugin: " + ChatColor.WHITE +
                (plugin.isQoLPluginPresent() ? ChatColor.GREEN + "Detected" : ChatColor.RED + "Not found"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("gaia.admin")) return new ArrayList<>();

        if (args.length == 1) {
            return Arrays.asList("debug", "violations", "reload", "alerts", "info").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("debug")
                || args[0].equalsIgnoreCase("violations")
                || args[0].equalsIgnoreCase("vl"))) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
