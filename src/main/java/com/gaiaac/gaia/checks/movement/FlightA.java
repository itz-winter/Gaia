package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Flight (A) - Detects airborne motion patterns inconsistent with gravity.
 * Vanilla gravity: -0.08 per tick, with 0.98 drag multiplier.
 * Predicted velocity: (lastDeltaY - 0.08) * 0.98
 */
public class FlightA extends Check {

    public FlightA(GaiaPlugin plugin) { super(plugin, "Flight", "A", "flight", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || data.isFlying() || data.isGliding() || data.isInVehicle()
                || data.isInWater() || data.isInLava() || data.isOnClimbable() || recentlyReceivedVelocity(data)) return;
        if (data.hasLevitation() || data.hasSlowFalling()) return;
        if (data.isRiptiding() || data.isInBubbleColumn()) return;

        double dY = data.getDeltaY();
        int airTicks = data.getAirTicks();

        if (data.isOnGround() || airTicks < 2) return;

        // Godbridge grace: player places a block at the edge and is briefly airborne (1-3 ticks)
        // before stepping onto the newly placed block. This is a legitimate movement pattern.
        if (airTicks <= 3 && System.currentTimeMillis() - data.getLastActualBlockPlaceTime() < 200) return;

        // Check 1: Ascending after initial jump phase.
        // Raised from > 6 to > 10: jump boost III/IV can keep dY positive until tick ~9.
        if (dY > 0.1 && airTicks > 10) {
            double buffer = data.addBuffer("flight_a_buffer", 2);
            if (buffer > 4) {
                flag(player, data, 3.0, "ascend dY=" + String.format("%.6f", dY) + " airTicks=" + airTicks);
                data.setBuffer("flight_a_buffer", 2);
            }
        }
        // Check 2: Gravity deviation — compare actual Y motion to predicted
        else if (airTicks > 3) {
            double predicted = data.getPredictedY();
            double offset = Math.abs(dY - predicted);
            // Allow generous tolerance for lag (0.1 + ping compensation)
            double tolerance = 0.1 + (data.getPing() / 500.0);

            if (offset > tolerance && dY > -0.5) {
                double buffer = data.addBuffer("flight_a_buffer", 1);
                if (buffer > 5) {
                    flag(player, data, 2.0, "gravity dY=" + String.format("%.6f", dY)
                            + " predicted=" + String.format("%.6f", predicted)
                            + " offset=" + String.format("%.6f", offset));
                    data.setBuffer("flight_a_buffer", 2);
                }
            } else {
                data.decreaseBuffer("flight_a_buffer", 0.5);
            }
        } else {
            data.decreaseBuffer("flight_a_buffer", 0.25);
        }
    }
}
