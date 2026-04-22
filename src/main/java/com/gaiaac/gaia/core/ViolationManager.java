package com.gaiaac.gaia.core;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.discord.DiscordWebhook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ViolationManager {

    private final GaiaPlugin plugin;
    private volatile double cachedTPS = 20.0;

    public ViolationManager(GaiaPlugin plugin) {
        this.plugin = plugin;
        // Update TPS cache every second on main thread — avoids reflection per flag()
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            try {
                java.lang.reflect.Method getTPS = Bukkit.class.getMethod("getTPS");
                cachedTPS = ((double[]) getTPS.invoke(null))[0];
            } catch (Exception ignored) {
                cachedTPS = 20.0;
            }
        }, 20L, 20L);
    }

    public void flag(Player player, Check check, String debugInfo) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        double vl = data.getVL(check.getFullCheckName());
        double threshold = check.getThreshold();

        // Send alert (AlertManager handles scheduling to main thread internally)
        plugin.getAlertManager().sendAlert(player, check, vl, threshold, debugInfo);

        // Send Discord alert (DiscordWebhook uses its own async executor)
        DiscordWebhook webhook = plugin.getDiscordWebhook();
        if (webhook != null && webhook.isEnabled() && plugin.getConfigManager().isDiscordEnabled()) {
            String location = String.format("%.1f, %.1f, %.1f",
                    data.getX(), data.getY(), data.getZ());

            webhook.sendAlert(
                    player.getName(),
                    check.getCheckName(),
                    check.getType(),
                    vl,
                    threshold,
                    debugInfo,
                    data.getPing(),
                    cachedTPS,
                    data.getClientBrand(),
                    data.getClientProtocolVersion(),
                    location
            );
        }

        // Check for punishment (executePunishment schedules to main thread)
        if (plugin.getConfigManager().isPunishmentEnabled() && vl >= threshold) {
            executePunishment(player, check, vl);
        }
    }

    public void decayAllViolations() {
        boolean wipeAll = plugin.getConfigManager().isDecayAll();
        double decayAmount = plugin.getConfigManager().getDecayAmount();
        for (PlayerData data : plugin.getPlayerDataManager().getAllPlayerData().values()) {
            if (wipeAll) {
                data.clearViolations();
            } else {
                data.decayViolations(decayAmount);
            }
        }

        // Broadcast decay message to staff with gaia.alerts permission (if configured)
        String rawMessage = plugin.getConfigManager().getDecayMessage();
        if (rawMessage != null && !rawMessage.isEmpty()) {
            String formatted = org.bukkit.ChatColor.translateAlternateColorCodes('&', rawMessage);
            Bukkit.getScheduler().runTask(plugin, () -> {
                for (org.bukkit.entity.Player staff : Bukkit.getOnlinePlayers()) {
                    if (staff.hasPermission("gaia.alerts")) {
                        staff.sendMessage(formatted);
                    }
                }
                // Always log to console
                plugin.getLogger().info(org.bukkit.ChatColor.stripColor(formatted));
            });
        }
    }

    /**
     * Get the cached TPS value. Updated once per second on the main thread.
     * Used by Check.isLowTPS() to avoid per-call reflection.
     */
    public double getCachedTPS() {
        return cachedTPS;
    }

    private void executePunishment(Player player, Check check, double vl) {
        String command = plugin.getConfigManager().getPunishmentCommand();
        if (command == null || command.isEmpty()) return;

        command = command.replace("{player}", player.getName())
                .replace("{check}", check.getCheckName())
                .replace("{type}", check.getType())
                .replace("{vl}", String.format("%.1f", vl))
                .replace("{threshold}", String.format("%.1f", check.getThreshold()));

        String finalCommand = command;
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
        });
    }
}
