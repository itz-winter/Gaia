package com.gaiaac.gaia.config;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * Configuration manager for Gaia Anticheat.
 * Handles loading/reloading config.yml and providing typed access to settings.
 */
public class ConfigManager {

    private final GaiaPlugin plugin;
    private FileConfiguration config;

    // Discord
    private String discordWebhookUrl;
    private boolean discordEnabled;

    // Alerts
    private boolean alertsEnabled;
    private boolean alertsConsole;
    private String alertFormat;

    // Punishment
    private boolean punishmentEnabled;
    private String punishmentCommand;

    // General
    private int decayIntervalSeconds;
    private double decayAmount;

    // Debug
    private boolean debugMode;
    private boolean verboseDebug;

    public ConfigManager(GaiaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        load();
    }

    public void load() {
        plugin.reloadConfig();
        config = plugin.getConfig();

        // Discord settings
        discordWebhookUrl = config.getString("discord.webhook-url", "");
        discordEnabled = config.getBoolean("discord.enabled", false);

        // Alert settings
        alertsEnabled = config.getBoolean("alerts.enabled", true);
        alertsConsole = config.getBoolean("alerts.console", true);
        alertFormat = config.getString("alerts.format",
                "&8Gaia &7» &f{player} &7failed &c{check} &7({type}) &7[&f{vl}&7/&f{threshold}&7]");

        // Punishment settings
        punishmentEnabled = config.getBoolean("punishment.enabled", true);
        punishmentCommand = config.getString("punishment.command", "kick {player} [Gaia] Unfair Advantage");

        // General settings
        decayIntervalSeconds = config.getInt("general.decay-interval", 300);
        decayAmount = config.getDouble("general.decay-amount", 1.0);

        // Debug settings
        debugMode = config.getBoolean("debug.enabled", false);
        verboseDebug = config.getBoolean("debug.verbose", false);

        // Load per-check settings
        if (plugin.getCheckManager() != null) {
            for (Check check : plugin.getCheckManager().getAllChecks()) {
                String path = "checks." + check.getCheckName().toLowerCase() + "."
                        + check.getType().toLowerCase();
                if (config.contains(path + ".enabled")) {
                    check.setEnabled(config.getBoolean(path + ".enabled", true));
                }
                if (config.contains(path + ".threshold")) {
                    check.setThreshold(config.getDouble(path + ".threshold", check.getThreshold()));
                }
            }
        }
    }

    public void reload() {
        load();
    }

    // Getters
    public String getDiscordWebhookUrl() { return discordWebhookUrl; }
    public boolean isDiscordEnabled() { return discordEnabled; }
    public boolean isAlertsEnabled() { return alertsEnabled; }
    public boolean isAlertsConsole() { return alertsConsole; }
    public String getAlertFormat() { return alertFormat; }
    public boolean isPunishmentEnabled() { return punishmentEnabled; }
    public String getPunishmentCommand() { return punishmentCommand; }
    public int getDecayIntervalSeconds() { return decayIntervalSeconds; }
    public double getDecayAmount() { return decayAmount; }
    public boolean isDebugMode() { return debugMode; }
    public boolean isVerboseDebug() { return verboseDebug; }
    public FileConfiguration getConfig() { return config; }

    /**
     * Check if a specific check is enabled in config.
     */
    public boolean isCheckEnabled(String checkName, String type) {
        String path = "checks." + checkName.toLowerCase() + "." + type.toLowerCase() + ".enabled";
        return config.getBoolean(path, true);
    }

    /**
     * Get the threshold for a specific check from config.
     */
    public double getCheckThreshold(String checkName, String type) {
        String path = "checks." + checkName.toLowerCase() + "." + type.toLowerCase() + ".threshold";
        return config.getDouble(path, -1);
    }
}
