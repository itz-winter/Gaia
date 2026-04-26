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

        // Schedule potion effect + attribute caching task — runs every 4 ticks (200ms) on main thread
        // Attributes (getValue()) capture ALL modifiers: potions, /attribute, plugin modifications, etc.
        // This avoids calling Bukkit API from netty/async threads (like Grim/Vulcan pattern)
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (PlayerData data : playerDataManager.getAllPlayerData().values()) {
                org.bukkit.entity.Player p = data.getPlayer();
                if (p == null || !p.isOnline()) continue;
                try {
                    // Cache speed effect (still used by some combat checks)
                    if (p.hasPotionEffect(org.bukkit.potion.PotionEffectType.SPEED)) {
                        org.bukkit.potion.PotionEffect eff = p.getPotionEffect(org.bukkit.potion.PotionEffectType.SPEED);
                        data.setSpeedAmplifier(eff != null ? eff.getAmplifier() : -1);
                    } else {
                        data.setSpeedAmplifier(-1);
                    }
                    // Cache jump boost effect (still used by some checks as fallback)
                    if (p.hasPotionEffect(org.bukkit.potion.PotionEffectType.JUMP_BOOST)) {
                        org.bukkit.potion.PotionEffect eff = p.getPotionEffect(org.bukkit.potion.PotionEffectType.JUMP_BOOST);
                        data.setJumpBoostAmplifier(eff != null ? eff.getAmplifier() : -1);
                    } else {
                        data.setJumpBoostAmplifier(-1);
                    }
                    // Cache levitation effect
                    data.setHasLevitation(p.hasPotionEffect(org.bukkit.potion.PotionEffectType.LEVITATION));
                    data.setHasSlowFalling(p.hasPotionEffect(org.bukkit.potion.PotionEffectType.SLOW_FALLING));
                    // Cache elytra wearing state — true if gliding OR elytra equipped in chest slot
                    // (setGliding is called later alongside lastGlideStartTime tracking)
                    org.bukkit.inventory.ItemStack chest = p.getInventory().getChestplate();
                    data.setWearingElytra(p.isGliding() ||
                            (chest != null && chest.getType() == org.bukkit.Material.ELYTRA));
                    // Cache riding-jumpable-vehicle state — horses/camels can jump, boats/minecarts cannot.
                    // Used to prevent BoatFly/EntityFlight false positives on legitimate horse jumps.
                    org.bukkit.entity.Entity vehicle = p.getVehicle();
                    data.setRidingJumpableVehicle(vehicle instanceof org.bukkit.entity.AbstractHorse
                            || vehicle instanceof org.bukkit.entity.Camel);
                    // Cache player attributes — getValue() returns the final value with ALL modifiers applied
                    // (potions, /attribute command, datapack effects, plugin modifications)
                    // Note: In Spigot 1.21.3+, Attribute was refactored from an enum to a typed interface;
                    // the GENERIC_/PLAYER_ prefixes were dropped (GENERIC_MOVEMENT_SPEED → MOVEMENT_SPEED, etc.)
                    org.bukkit.attribute.AttributeInstance moveSpeed = p.getAttribute(org.bukkit.attribute.Attribute.MOVEMENT_SPEED);
                    if (moveSpeed != null) data.setMovementSpeedAttribute(moveSpeed.getValue());
                    // JUMP_STRENGTH and GRAVITY were added in MC 1.21 — use try/catch for safety on older builds
                    try {
                        org.bukkit.attribute.AttributeInstance jumpStrength = p.getAttribute(org.bukkit.attribute.Attribute.JUMP_STRENGTH);
                        if (jumpStrength != null) data.setJumpStrengthAttribute(jumpStrength.getValue());
                    } catch (NoSuchFieldError | Exception ignored) { /* pre-1.21 server */ }
                    try {
                        org.bukkit.attribute.AttributeInstance gravity = p.getAttribute(org.bukkit.attribute.Attribute.GRAVITY);
                        if (gravity != null) data.setGravityAttribute(gravity.getValue());
                    } catch (NoSuchFieldError | Exception ignored) { /* pre-1.21 server */ }
                    // SNEAKING_SPEED — includes Swift Sneak enchantment (default 0.3, Swift Sneak III → 0.75)
                    try {
                        org.bukkit.attribute.AttributeInstance sneakSpeed = p.getAttribute(org.bukkit.attribute.Attribute.SNEAKING_SPEED);
                        if (sneakSpeed != null) data.setSneakingSpeedAttribute(sneakSpeed.getValue());
                    } catch (NoSuchFieldError | Exception ignored) { /* pre-1.21 server */ }
                    // STEP_HEIGHT — the block height a player can walk over (default 0.6, plugins/mods can change)
                    try {
                        org.bukkit.attribute.AttributeInstance stepHeight = p.getAttribute(org.bukkit.attribute.Attribute.STEP_HEIGHT);
                        if (stepHeight != null) data.setStepHeightAttribute(stepHeight.getValue());
                    } catch (NoSuchFieldError | Exception ignored) { /* pre-1.21 server */ }
                    // Cache environment/block states (all Bukkit world API — main thread only)
                    boolean wasInWater = data.isInWater();
                    boolean wasGliding = data.isGliding();
                    data.setInWater(p.isInWater());
                    // Water entry: track when player entered water (used to gate exit grace below)
                    if (!wasInWater && data.isInWater()) {
                        data.setLastEnterWaterTime(System.currentTimeMillis());
                    }
                    // Detect water→air transition for leap-out grace (JesusD/E, Flight FP prevention).
                    // ONLY grant exit grace if the player was submerged for >1500ms — prevents Jesus
                    // hack users from exploiting the grace by briefly skimming the water surface.
                    if (wasInWater && !data.isInWater()) {
                        long submergedDuration = System.currentTimeMillis() - data.getLastEnterWaterTime();
                        if (submergedDuration > 1500) {
                            data.setLastExitWaterTime(System.currentTimeMillis());
                        }
                    }
                    // Elytra glide start: grace period for MotionB during initial elytra deployment
                    data.setGliding(p.isGliding());
                    if (!wasGliding && data.isGliding()) {
                        data.setLastGlideStartTime(System.currentTimeMillis());
                    }
                    // Cache sleeping state — aim checks must skip while player is in bed
                    data.setSleeping(p.isSleeping());
                    data.setSwimming(p.isSwimming());
                    // Cache dead/handRaised — Bukkit API; read from netty thread via data (thread-safe)
                    data.setDead(p.isDead());
                    data.setHandRaised(p.isHandRaised());
                    // Cache vehicle state — isInsideVehicle() is Bukkit API (main thread only)
                    data.setInVehicle(p.isInsideVehicle());
                    // Lava: check the block occupying the player's body (eye location for head-in-lava)
                    org.bukkit.block.Block bodyBlock = p.getLocation().getBlock();
                    data.setInLava(bodyBlock.getType() == org.bukkit.Material.LAVA);
                    // Climbable: use Bukkit's CLIMBABLE tag (ladders, vines, scaffolding, etc.)
                    data.setOnClimbable(org.bukkit.Tag.CLIMBABLE.isTagged(bodyBlock.getType()));
                    // Ice/Slime: check the block directly below the player
                    org.bukkit.block.Block below = p.getLocation().subtract(0, 0.1, 0).getBlock();
                    org.bukkit.Material belowType = below.getType();
                    data.setOnIce(belowType == org.bukkit.Material.ICE
                            || belowType == org.bukkit.Material.PACKED_ICE
                            || belowType == org.bukkit.Material.BLUE_ICE
                            || belowType == org.bukkit.Material.FROSTED_ICE);
                    data.setOnSlime(belowType == org.bukkit.Material.SLIME_BLOCK);
                    // Honey block: reduces movement speed and jump height
                    data.setOnHoneyBlock(belowType == org.bukkit.Material.HONEY_BLOCK);
                    // Soul block: Soul Speed enchantment increases speed on soul sand/soul soil
                    data.setOnSoulBlock(belowType == org.bukkit.Material.SOUL_SAND
                            || belowType == org.bukkit.Material.SOUL_SOIL);
                    // Bubble column: propels player up/down rapidly — must exempt from flight/motion checks
                    data.setInBubbleColumn(bodyBlock.getType() == org.bukkit.Material.BUBBLE_COLUMN);
                    // Riptide: trident launches player at high speed through rain/water
                    data.setRiptiding(p.isRiptiding());
                    // Refresh permissions periodically (player may gain/lose perms via LP, etc.)
                    boolean wasExempt = data.isExempt();
                    boolean explicitDeny = p.isPermissionSet("gaia.bypass") && !p.hasPermission("gaia.bypass");
                    data.setBypassed(!explicitDeny && (p.isOp() || p.hasPermission("gaia.bypass")));
                    data.setAlertsPermission(p.hasPermission("gaia.alerts"));
                    // If player just became non-exempt (e.g. bypass config toggled off), reset position
                    // tracking so VClip/movement checks don't flag the stale position delta on first packet.
                    // (While exempt, onPacketReceive returns early and handleMovement is never called.)
                    if (wasExempt && !data.isExempt()) {
                        data.setLastTeleportTime(System.currentTimeMillis());
                    }
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
