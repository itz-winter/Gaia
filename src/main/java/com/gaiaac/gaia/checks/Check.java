package com.gaiaac.gaia.checks;

import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

public abstract class Check {

    protected final GaiaPlugin plugin;
    private final String checkName;
    private final String type;
    private final String checkCategory;
    private final String fullCheckName; // Pre-cached — avoid string concat on every call
    private boolean enabled;
    private double threshold;
    private String bypassPermission;

    public Check(GaiaPlugin plugin, String checkName, String type, String checkCategory,
                 boolean enabled, double threshold) {
        this.plugin = plugin;
        this.checkName = checkName;
        this.type = type;
        this.checkCategory = checkCategory.toLowerCase();
        this.fullCheckName = checkName + " (" + type + ")"; // Cache once at construction
        this.enabled = enabled;
        this.threshold = threshold;
        this.bypassPermission = "gaia." + checkName.toLowerCase() + ".bypass";
    }

    /**
     * Process the check for a player. Implementations should call flag() if a violation is detected.
     */
    public abstract void handle(Player player, PlayerData data);

    /**
     * Whether this check has a real implementation. Stub checks override this to return false,
     * and will be excluded from per-packet dispatch to avoid wasted cycles.
     */
    public boolean isImplemented() {
        return true;
    }

    /**
     * Flag a player for a violation with a specific VL amount.
     */
    protected void flag(Player player, PlayerData data, double vlAmount, String debugInfo) {
        // Use cached permission + gamemode from PlayerData — thread-safe, no Bukkit API call on netty thread
        if (data.isExempt()) return;

        data.addVL(getFullCheckName(), vlAmount);
        double vl = data.getVL(getFullCheckName());

        // Only alert if above a minimum threshold to reduce spam
        if (vl >= 1.0) {
            plugin.getViolationManager().flag(player, this, debugInfo);
        }

        // Debug output
        if (data.isDebugMode()) {
            plugin.getAlertManager().sendDebugMessage(player,
                    getFullCheckName() + " | VL: " + String.format("%.1f", vl) + " | " + debugInfo);
        }
    }

    /**
     * Flag a player for a violation with default VL increment of 1.
     */
    protected void flag(Player player, PlayerData data, String debugInfo) {
        flag(player, data, 1.0, debugInfo);
    }

    /**
     * Check if TPS is too low to reliably detect cheats.
     * Uses the cached TPS value from ViolationManager — no reflection overhead.
     */
    protected boolean isLowTPS() {
        return plugin.getViolationManager().getCachedTPS() < 18.0;
    }

    /**
     * Get lag-compensated threshold.
     */
    protected double getCompensatedThreshold(PlayerData data) {
        return threshold + (data.getPing() / 50.0);
    }

    /**
     * Check if enough time has passed since teleport to validate movement.
     */
    protected boolean recentlyTeleported(PlayerData data) {
        return System.currentTimeMillis() - data.getLastTeleportTime() < 1000;
    }

    /**
     * Check if the player recently received velocity (knockback).
     */
    protected boolean recentlyReceivedVelocity(PlayerData data) {
        return System.currentTimeMillis() - data.getLastVelocityTime() < 1000;
    }

    /**
     * Check if the player just joined.
     */
    protected boolean recentlyJoined(PlayerData data) {
        return System.currentTimeMillis() - data.getJoinTime() < 3000;
    }

    public String getCheckName() { return checkName; }
    public String getType() { return type; }
    public String getCheckCategory() { return checkCategory; }
    public String getFullCheckName() { return fullCheckName; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public double getThreshold() { return threshold; }
    public void setThreshold(double threshold) { this.threshold = threshold; }
    public String getBypassPermission() { return bypassPermission; }
    public void setBypassPermission(String bypassPermission) { this.bypassPermission = bypassPermission; }
}
