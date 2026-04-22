package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Prediction (A) — Y-velocity diverges from independent physics simulation.
 *
 * Unlike FlightA which predicts based on the ACTUAL previous deltaY (chasing),
 * this check uses simVY — a simulation that is initialised once per jump and
 * then evolves purely under physics WITHOUT reading the player's actual position.
 *
 * Why this matters: a flying cheater's actual deltaY stays near 0 while the
 * simulation predicts steadily decreasing Y (gravity). FlightA's chasing approach
 * only catches the single-tick divergence each packet; simVY's independent approach
 * accumulates divergence over multiple ticks, making it much easier to detect
 * hover / slow-fall cheats that stay just inside FlightA's per-tick tolerance.
 *
 * Works in ALL media — air, water, lava — using per-environment physics constants.
 * The simulation is automatically invalidated for bubble columns, riptide, levitation,
 * server velocity packets, gliding, vehicles, and climbables.
 */
public class PredictionA extends Check {

    // Base tolerance (blocks/tick); environment and ping adjustments added on top
    private static final double TOLERANCE_AIR   = 0.06;
    private static final double TOLERANCE_WATER = 0.15; // water physics are noisier
    private static final double TOLERANCE_LAVA  = 0.20;

    public PredictionA(GaiaPlugin plugin) {
        super(plugin, "Prediction", "A", "prediction", true, 10);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        // Engine sets simYValid=false for all untrackable states
        if (!data.isSimYValid()) return;

        if (recentlyTeleported(data) || recentlyJoined(data)) return;
        if (recentlyReceivedVelocity(data)) return;
        // Safety-belt: skip the same states the engine already handles, in case the
        // 4L cache is momentarily stale and the engine ran before the flag was set.
        if (data.isFlying() || data.isGliding() || data.isInVehicle()) return;
        if (data.isOnGround() || data.wasOnGround()) return; // skip transition ticks

        double actual    = data.getDeltaY();
        double simulated = data.getSimVY();
        double offset    = Math.abs(actual - simulated);

        // Per-environment tolerance + generous ping allowance
        double tolerance;
        if (data.isInWater()) {
            tolerance = TOLERANCE_WATER + data.getPing() / 800.0;
        } else if (data.isInLava()) {
            tolerance = TOLERANCE_LAVA + data.getPing() / 800.0;
        } else {
            tolerance = TOLERANCE_AIR + data.getPing() / 1000.0;
        }

        // Also add a fraction of the simulation's current magnitude to allow
        // slight real-world drag variance and step rounding at high speeds
        tolerance += Math.abs(simulated) * 0.05;

        if (offset > tolerance) {
            double buffer = data.addBuffer("pred_a_buf", 1);
            if (buffer > 4) {
                flag(player, data, 2.5, "yDivergence=" + String.format("%.5f", offset)
                        + " actual=" + String.format("%.5f", actual)
                        + " sim=" + String.format("%.5f", simulated)
                        + " tol=" + String.format("%.5f", tolerance)
                        + " airTicks=" + data.getAirTicks());
                data.setBuffer("pred_a_buf", 2);
            }
        } else {
            data.decreaseBuffer("pred_a_buf", 0.33);
        }
    }
}
