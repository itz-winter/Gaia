package com.gaiaac.gaia.core;

import com.gaiaac.gaia.checks.Check;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AlertManager {

    private final GaiaPlugin plugin;
    private static final String PREFIX = ChatColor.DARK_GRAY + "Gaia " + ChatColor.GRAY + "» " + ChatColor.RESET;
    private static final String DEBUG_PREFIX = ChatColor.DARK_GRAY + "Gaia " + ChatColor.DARK_AQUA + "[DEBUG] " + ChatColor.RESET;
    private final Set<UUID> alertsDisabled = new HashSet<>();

    // Rate limiting: max 1 alert per check per player per 500ms — prevents main-thread scheduling spam
    private final Map<String, Long> lastAlertTime = new ConcurrentHashMap<>();
    private static final long ALERT_COOLDOWN_MS = 500;

    // Per-player watching: admins watching a specific target get continuous debug output.
    // Key = target UUID, Value = set of admin UUIDs watching that target.
    private final Map<UUID, Set<UUID>> watchers = new ConcurrentHashMap<>();

    // Global debug mode toggle (runtime, per-session — separate from config debug.enabled)
    private volatile boolean globalDebugMode = false;

    public AlertManager(GaiaPlugin plugin) {
        this.plugin = plugin;
    }

    public void sendAlert(Player flaggedPlayer, Check check, double vl, double threshold, String debugInfo) {
        // Rate limit alerts per player+check combination
        String alertKey = flaggedPlayer.getUniqueId().toString() + ":" + check.getFullCheckName();
        long now = System.currentTimeMillis();
        Long lastTime = lastAlertTime.get(alertKey);
        if (lastTime != null && now - lastTime < ALERT_COOLDOWN_MS) {
            // Even if rate-limited for chat, still log to debug watchers if any
            dispatchDebugToWatchers(flaggedPlayer, check, vl, threshold, debugInfo);
            return;
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

        // Verbose console debug — logs full detail every time a flag event happens
        if (plugin.getConfigManager().isDebugMode() || globalDebugMode) {
            PlayerData data = plugin.getPlayerDataManager().getPlayerData(flaggedPlayer.getUniqueId());
            String detail = buildDetailString(flaggedPlayer, data, check, vl, threshold, debugInfo);
            plugin.getLogger().info("[DEBUG] " + detail);
        }

        // Player alerts — must run on main thread (Bukkit API not thread-safe)
        if (plugin.getConfigManager().isAlertsEnabled()) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                for (Player staff : Bukkit.getOnlinePlayers()) {
                    PlayerData staffData = plugin.getPlayerDataManager().getPlayerData(staff.getUniqueId());
                    boolean hasAlerts = staffData != null ? staffData.hasAlertsPermission() : staff.hasPermission("gaia.alerts");
                    if (hasAlerts && !alertsDisabled.contains(staff.getUniqueId())) {
                        staff.sendMessage(message);
                    }
                }
            });
        }

        // Send to per-player debug watchers
        dispatchDebugToWatchers(flaggedPlayer, check, vl, threshold, debugInfo);
    }

    /**
     * Send a debug message to all admins watching the given target player.
     * This is called from the netty thread — actual send is scheduled to main thread.
     */
    public void sendDebugToWatchers(Player target, String message) {
        Set<UUID> watching = watchers.get(target.getUniqueId());
        if (watching == null || watching.isEmpty()) return;

        String formatted = DEBUG_PREFIX + ChatColor.GRAY + "[" + ChatColor.AQUA + target.getName()
                + ChatColor.GRAY + "] " + ChatColor.WHITE + message;

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (UUID watcherUUID : watching) {
                Player watcher = Bukkit.getPlayer(watcherUUID);
                if (watcher != null && watcher.isOnline()) {
                    watcher.sendMessage(formatted);
                }
            }
        });
    }

    private void dispatchDebugToWatchers(Player flaggedPlayer, Check check, double vl, double threshold, String debugInfo) {
        Set<UUID> watching = watchers.get(flaggedPlayer.getUniqueId());
        if (watching == null || watching.isEmpty()) return;

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(flaggedPlayer.getUniqueId());
        String detail = buildDetailString(flaggedPlayer, data, check, vl, threshold, debugInfo);

        String msg = DEBUG_PREFIX + ChatColor.GRAY + "[" + ChatColor.RED + "FLAG" + ChatColor.GRAY + "] "
                + ChatColor.WHITE + detail;

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (UUID watcherUUID : watching) {
                Player watcher = Bukkit.getPlayer(watcherUUID);
                if (watcher != null && watcher.isOnline()) {
                    watcher.sendMessage(msg);
                }
            }
        });
    }

    /**
     * Builds a full detail string about a flag event for debug output.
     */
    private String buildDetailString(Player player, PlayerData data, Check check, double vl, double threshold, String debugInfo) {
        if (data == null) {
            return check.getFullCheckName() + " VL=" + String.format("%.1f", vl) + "/" + threshold
                    + " | " + debugInfo + " | (no data)";
        }
        return check.getFullCheckName()
                + " VL=" + String.format("%.1f", vl) + "/" + threshold
                + " | " + debugInfo
                + " | pos=" + String.format("%.2f,%.2f,%.2f", data.getX(), data.getY(), data.getZ())
                + " | dXZ=" + String.format("%.4f", data.getDeltaXZ())
                + " | dY=" + String.format("%.4f", data.getDeltaY())
                + " | dYaw=" + String.format("%.3f", data.getDeltaYaw())
                + " | dPitch=" + String.format("%.3f", data.getDeltaPitch())
                + " | onGround=" + data.isOnGround()
                + " | airTicks=" + data.getAirTicks()
                + " | sprinting=" + data.isSprinting()
                + " | flying=" + data.isFlying()
                + " | gliding=" + data.isGliding()
                + " | ping=" + data.getPing() + "ms"
                + " | tps=" + String.format("%.1f", plugin.getViolationManager().getCachedTPS());
    }

    /**
     * Start watching a target player — all flag events will be sent to the watcher.
     * @return true if now watching, false if was already watching (and removed)
     */
    public boolean toggleWatch(UUID watcher, UUID target) {
        Set<UUID> watching = watchers.computeIfAbsent(target, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
        if (watching.contains(watcher)) {
            watching.remove(watcher);
            if (watching.isEmpty()) watchers.remove(target);
            return false;
        } else {
            watching.add(watcher);
            return true;
        }
    }

    public boolean isWatching(UUID watcher, UUID target) {
        Set<UUID> watching = watchers.get(target);
        return watching != null && watching.contains(watcher);
    }

    /** Remove all watches for a player (e.g. on disconnect). */
    public void removeAllWatches(UUID uuid) {
        watchers.remove(uuid);
        watchers.values().forEach(set -> set.remove(uuid));
    }

    public boolean toggleGlobalDebugMode() {
        globalDebugMode = !globalDebugMode;
        return globalDebugMode;
    }

    public boolean isGlobalDebugMode() {
        return globalDebugMode;
    }

    public void sendDebugMessage(Player staff, String message) {
        staff.sendMessage(DEBUG_PREFIX + ChatColor.WHITE + message);
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

