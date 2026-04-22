package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Flight (C) - Detects gravity inconsistency (falling too slowly).
 * After 15 air ticks in vanilla, a player should be falling at approximately -1.5+ b/t.
 * If dY is still above -0.3 at this point without Slow Falling or Levitation, it's suspicious.
 * Catches anti-gravity and sustained glide hacks that stay below FlightA/B's detection.
 */
public class FlightC extends Check {
    public FlightC(GaiaPlugin plugin) { super(plugin, "Flight", "C", "flight", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || data.isFlying() || data.isGliding() || data.isInVehicle()) return;
        if (data.isInWater() || data.isInLava() || data.isOnClimbable() || recentlyReceivedVelocity(data)) return;
        // Slow Falling and Levitation legitimately reduce gravity — skip
        if (data.hasLevitation() || data.hasSlowFalling() || data.isOnGround()) return;
        if (data.isRiptiding() || data.isInBubbleColumn()) return;

        int airTicks = data.getAirTicks();
        // Raised from 15 to 20 to avoid edge cases with high jump boost + lag
        if (airTicks < 20) return;

        double dY = data.getDeltaY();
        // At airTick 20 with normal gravity, expected dY ≈ -1.2 b/t.
        // Tightened from -0.30 to -0.20: only flag players falling < 0.20 b/t (clearly not normal gravity)
        if (dY > -0.20 && dY < 0.10) {
            double buffer = data.addBuffer("flight_c_buf", 1);
            if (buffer > 3) {
                flag(player, data, 2.0, "slowFall dY=" + String.format("%.4f", dY)
                        + " airTicks=" + airTicks);
                data.setBuffer("flight_c_buf", 1);
            }
        } else {
            data.decreaseBuffer("flight_c_buf", 0.5);
        }
    }
}
