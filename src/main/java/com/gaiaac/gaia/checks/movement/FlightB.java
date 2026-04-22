package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Flight (B) - Detects hover (no Y movement while airborne). */
public class FlightB extends Check {
    public FlightB(GaiaPlugin plugin) { super(plugin, "Flight", "B", "flight", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || data.isFlying() || data.isGliding() || data.isInVehicle()
                || data.isInWater() || data.isInLava() || data.isOnClimbable() || recentlyReceivedVelocity(data)) return;
        if (data.hasLevitation() || data.hasSlowFalling() || data.isRiptiding() || data.isInBubbleColumn()) return;

        if (Math.abs(data.getDeltaY()) < 0.005 && data.getAirTicks() > 15 && !data.isOnGround()) {
            double buffer = data.addBuffer("flight_b_buffer", 1);
            if (buffer > 5) {
                flag(player, data, 3.0, "hover dY=" + String.format("%.6f", data.getDeltaY()) + " airTicks=" + data.getAirTicks());
                data.setBuffer("flight_b_buffer", 2);
            }
        } else {
            data.decreaseBuffer("flight_b_buffer", 0.5);
        }
    }
}
