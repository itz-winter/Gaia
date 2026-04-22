package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Flight (D) - Detects glide (slow fall without Slow Falling effect).
 * Slow Falling changes gravity from 0.08 to 0.01 per tick (drag still 0.98).
 * If a player's vertical motion precisely matches slow-fall physics without having the effect,
 * they are faking it via a "slow fall" or "anti-gravity" cheat.
 */
public class FlightD extends Check {
    public FlightD(GaiaPlugin plugin) { super(plugin, "Flight", "D", "flight", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || data.isFlying() || data.isGliding() || data.isInVehicle()) return;
        if (data.isInWater() || data.isInLava() || data.isOnClimbable() || recentlyReceivedVelocity(data)) return;
        // Slow Falling and Levitation legitimately produce these physics patterns — skip
        if (data.hasLevitation() || data.hasSlowFalling() || data.isOnGround()) return;
        if (data.isRiptiding() || data.isInBubbleColumn()) return;

        int airTicks = data.getAirTicks();
        // Raised from 5 to 18: normal jump deceleration is well-established by then
        if (airTicks < 18) return;

        double dY = data.getDeltaY();
        double lastDY = data.getLastDeltaY();
        // Slow fall predicted: (lastDY - 0.01) * 0.98
        double slowFallPredicted = (lastDY - 0.01) * 0.98;
        double deviationFromSlowFall = Math.abs(dY - slowFallPredicted);

        // Tightened dY range to -0.20 (was -0.25) and deviation to 0.012 (was 0.015)
        if (deviationFromSlowFall < 0.012 && dY > -0.20 && dY < 0.05) {
            double buffer = data.addBuffer("flight_d_buf", 1);
            if (buffer > 4) {
                flag(player, data, 2.0, "fakeSlowFall dY=" + String.format("%.4f", dY)
                        + " slowFallPredicted=" + String.format("%.4f", slowFallPredicted)
                        + " airTicks=" + airTicks);
                data.setBuffer("flight_d_buf", 2);
            }
        } else {
            data.decreaseBuffer("flight_d_buf", 0.5);
        }
    }
}
