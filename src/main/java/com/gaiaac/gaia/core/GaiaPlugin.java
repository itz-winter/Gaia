package com.gaiaac.gaia.core;

import com.gaiaac.gaia.checks.CheckManager;
import com.gaiaac.gaia.commands.GaiaCommand;
import com.gaiaac.gaia.config.ConfigManager;
import com.gaiaac.gaia.discord.DiscordWebhook;
import com.gaiaac.gaia.listeners.PlayerDataListener;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class GaiaPlugin extends JavaPlugin {

    private static GaiaPlugin instance;
    private ConfigManager configManager;
    private CheckManager checkManager;
    private ViolationManager violationManager;
    private PlayerDataManager playerDataManager;
    private PacketManager packetManager;
    private DiscordWebhook discordWebhook;
    private AlertManager alertManager;
    private boolean qolPluginPresent = false;

    @Override
    public void onLoad() {
        instance = this;
        // PacketEvents must be loaded in onLoad
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings()
                .reEncodeByDefault(true)
                .checkForUpdates(false);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();

        // Initialize managers
        playerDataManager = new PlayerDataManager();
        violationManager = new ViolationManager(this);
        alertManager = new AlertManager(this);

        // Initialize config (after managers so checks can reference them)
        configManager = new ConfigManager(this);

        // Initialize check manager
        checkManager = new CheckManager(this);

        // Initialize Discord
        String webhookUrl = configManager.getDiscordWebhookUrl();
        discordWebhook = new DiscordWebhook(webhookUrl != null ? webhookUrl : "");

        // Initialize PacketEvents listeners
        packetManager = new PacketManager(this);
        PacketEvents.getAPI().getEventManager().registerListener(packetManager, PacketListenerPriority.NORMAL);
        PacketEvents.getAPI().init();

        // Register Bukkit listeners
        Bukkit.getPluginManager().registerEvents(new PlayerDataListener(this), this);

        // Register commands
        GaiaCommand gaiaCmd = new GaiaCommand(this);
        getCommand("gaia").setExecutor(gaiaCmd);
        getCommand("gaia").setTabCompleter(gaiaCmd);

        // Check for QoLPlugin
        if (Bukkit.getPluginManager().getPlugin("QoLPlugin") != null) {
            qolPluginPresent = true;
            getLogger().info("QoLPlugin detected - enabling compatibility support.");
        }

        // Schedule violation decay task
        int decayInterval = configManager.getDecayIntervalSeconds();
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            violationManager.decayAllViolations();
        }, 20L * decayInterval, 20L * decayInterval);

        // Schedule potion effect caching task — runs every 4 ticks (200ms) on main thread
        // This avoids calling Bukkit potion API from netty/async threads (like Grim/Vulcan pattern)
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (PlayerData data : playerDataManager.getAllPlayerData().values()) {
                org.bukkit.entity.Player p = data.getPlayer();
                if (p == null || !p.isOnline()) continue;
                try {
                    // Cache speed effect
                    if (p.hasPotionEffect(org.bukkit.potion.PotionEffectType.SPEED)) {
                        org.bukkit.potion.PotionEffect eff = p.getPotionEffect(org.bukkit.potion.PotionEffectType.SPEED);
                        data.setSpeedAmplifier(eff != null ? eff.getAmplifier() : -1);
                    } else {
                        data.setSpeedAmplifier(-1);
                    }
                    // Cache jump boost effect
                    if (p.hasPotionEffect(org.bukkit.potion.PotionEffectType.JUMP_BOOST)) {
                        org.bukkit.potion.PotionEffect eff = p.getPotionEffect(org.bukkit.potion.PotionEffectType.JUMP_BOOST);
                        data.setJumpBoostAmplifier(eff != null ? eff.getAmplifier() : -1);
                    } else {
                        data.setJumpBoostAmplifier(-1);
                    }
                    // Cache levitation effect
                    data.setHasLevitation(p.hasPotionEffect(org.bukkit.potion.PotionEffectType.LEVITATION));
                    // Also refresh permissions periodically (player may gain/lose perms)
                    data.setBypassed(p.isOp() || p.hasPermission("gaia.bypass"));
                    data.setAlertsPermission(p.hasPermission("gaia.alerts"));
                } catch (Exception ignored) {}
            }
        }, 4L, 4L);

        long elapsed = System.currentTimeMillis() - start;
        getLogger().info("Gaia Anticheat v" + getDescription().getVersion() + " enabled in " + elapsed + "ms");
    }

    @Override
    public void onDisable() {
        if (PacketEvents.getAPI() != null) {
            PacketEvents.getAPI().terminate();
        }
        if (discordWebhook != null) {
            discordWebhook.shutdown();
        }
        if (playerDataManager != null) {
            playerDataManager.cleanup();
        }
        getLogger().info("Gaia Anticheat disabled.");
    }

    public static GaiaPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CheckManager getCheckManager() {
        return checkManager;
    }

    public ViolationManager getViolationManager() {
        return violationManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public PacketManager getPacketManager() {
        return packetManager;
    }

    public DiscordWebhook getDiscordWebhook() {
        return discordWebhook;
    }

    public AlertManager getAlertManager() {
        return alertManager;
    }

    public boolean isQolPluginPresent() {
        return qolPluginPresent;
    }

    public boolean isQoLPluginPresent() {
        return qolPluginPresent;
    }
}
