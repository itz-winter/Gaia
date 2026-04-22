package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Flight (E) - Detects creative-like flight in survival. */
public class FlightE extends Check {
    public FlightE(GaiaPlugin plugin) { super(plugin, "Flight", "E", "flight", true, 10); }
    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || data.isFlying() || data.isGliding() || data.isInVehicle()) return;
        if (data.isInWater() || data.isInLava() || data.isOnClimbable() || data.isSwimming()) return;
        if (data.hasLevitation() || data.hasSlowFalling() || recentlyReceivedVelocity(data)) return;
        if (data.isRiptiding() || data.isInBubbleColumn()) return;
        // Constant Y increase without decrease
        if (data.getDeltaY() > 0.3 && data.getAirTicks() > 15) {
            flag(player, data, 3.0, "creativeFlight dY=" + String.format("%.4f", data.getDeltaY()) + " airTicks=" + data.getAirTicks());
        }
    }
}
