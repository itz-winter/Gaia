package com.gaiaac.gaia.core;

import com.gaiaac.gaia.checks.Check;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AlertManager {

    private final GaiaPlugin plugin;
    private static final String PREFIX = ChatColor.DARK_GRAY + "Gaia " + ChatColor.GRAY + "» " + ChatColor.RESET;
    private final Set<UUID> alertsDisabled = new HashSet<>();

    public AlertManager(GaiaPlugin plugin) {
        this.plugin = plugin;
    }

    public void sendAlert(Player flaggedPlayer, Check check, double vl, double threshold, String debugInfo) {
        String message = PREFIX + ChatColor.RED + flaggedPlayer.getName()
                + ChatColor.GRAY + " failed "
                + ChatColor.GOLD + check.getCheckName() + " (" + check.getType() + ")"
                + ChatColor.GRAY + " [" + ChatColor.RED + String.format("%.0f", vl)
                + ChatColor.GRAY + "/" + ChatColor.GREEN + String.format("%.0f", threshold) + ChatColor.GRAY + "]";

        // Console alert (logger is thread-safe)
        if (plugin.getConfigManager().isAlertsConsole()) {
            plugin.getLogger().info(ChatColor.stripColor(message));
        }

        // Player alerts — must run on main thread (Bukkit API not thread-safe)
        if (plugin.getConfigManager().isAlertsEnabled()) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                for (Player staff : Bukkit.getOnlinePlayers()) {
                    if (staff.hasPermission("gaia.alerts") && !alertsDisabled.contains(staff.getUniqueId())) {
                        staff.sendMessage(message);
                    }
                }
            });
        }
    }

    public void sendDebugMessage(Player staff, String message) {
        staff.sendMessage(PREFIX + ChatColor.AQUA + message);
    }

    /**
     * Toggle alert notifications for a player.
     * @return true if alerts are now enabled, false if disabled
     */
    public boolean toggleAlerts(Player player) {
        UUID uuid = player.getUniqueId();
        if (alertsDisabled.contains(uuid)) {
            alertsDisabled.remove(uuid);
            return true;
        } else {
            alertsDisabled.add(uuid);
            return false;
        }
    }
}
