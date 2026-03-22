package com.gaiaac.gaia.core;

import com.gaiaac.gaia.checks.Check;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AlertManager {

    private final GaiaPlugin plugin;
    private static final String PREFIX = ChatColor.DARK_GRAY + "Gaia " + ChatColor.GRAY + "» " + ChatColor.RESET;
    private final Set<UUID> alertsDisabled = new HashSet<>();

    // Rate limiting: max 1 alert per check per player per 500ms — prevents main-thread scheduling spam
    private final Map<String, Long> lastAlertTime = new ConcurrentHashMap<>();
    private static final long ALERT_COOLDOWN_MS = 500;

    public AlertManager(GaiaPlugin plugin) {
        this.plugin = plugin;
    }

    public void sendAlert(Player flaggedPlayer, Check check, double vl, double threshold, String debugInfo) {
        // Rate limit alerts per player+check combination
        String alertKey = flaggedPlayer.getUniqueId().toString() + ":" + check.getFullCheckName();
        long now = System.currentTimeMillis();
        Long lastTime = lastAlertTime.get(alertKey);
        if (lastTime != null && now - lastTime < ALERT_COOLDOWN_MS) {
            return; // Skip this alert — too soon since last one for same player+check
        }
        lastAlertTime.put(alertKey, now);

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
                    // Use cached permission from PlayerData when available to avoid permission checks
                    PlayerData staffData = plugin.getPlayerDataManager().getPlayerData(staff.getUniqueId());
                    boolean hasAlerts = staffData != null ? staffData.hasAlertsPermission() : staff.hasPermission("gaia.alerts");
                    if (hasAlerts && !alertsDisabled.contains(staff.getUniqueId())) {
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
