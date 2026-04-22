package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Prediction (B) — horizontal speed exceeds physics-predicted maximum.
 *
 * Complementary to SpeedA:
 *  • SpeedA  — uses friction-based momentum rolloff per tick; great for ground acceleration spikes
 *  • PredictionB — uses the engine's attribute-derived absolute ceiling; great for sustained
 *                  over-speed (speed-hack, NoSlow bypass at sprint speed, bhop that slips
 *                  past SpeedB's 4-tick window)
 *
 * The engine computes maxPredictedVXZ based on:
 *   movement speed attribute (captures ALL potion / plugin modifiers),
 *   sprint / sneak state, water / lava drag, honey block slow, and ping tolerance.
 *
 * Skips: ice, slime (surface momentum accumulation), flying, gliding, riptide,
 * bubble column, vehicle, recently received velocity.
 *
 * Uses a decay buffer — requires several consecutive over-speed ticks before flagging
 * to tolerate single-packet lag spikes and attribute-cache lag.
 */
public class PredictionB extends Check {

    public PredictionB(GaiaPlugin plugin) {
        super(plugin, "Prediction", "B", "speed", true, 10);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;
        if (recentlyReceivedVelocity(data)) return;
        // Engine sets MAX_VALUE for states it can't bound — skip those
        double maxVXZ = data.getMaxPredictedVXZ();
        if (maxVXZ >= Double.MAX_VALUE / 2) return;

        // Belt-and-suspenders: re-check states that map to MAX_VALUE in the engine
        if (data.isFlying() || data.isGliding() || data.isRiptiding()) return;
        if (data.isInVehicle() || data.isOnIce() || data.isOnSlime()) return;

        double actualVXZ = data.getDeltaXZ();
        if (actualVXZ <= maxVXZ) {
            data.decreaseBuffer("pred_b_buf", 0.33);
            return;
        }

        // How far over the predicted maximum is the player?
        double overshoot = actualVXZ / maxVXZ; // 1.0 = at max, 1.3 = 30% over, etc.

        // Require at least 20% over max before accumulating the buffer.
        // This absorbs attribute-cache lag (4L = up to 200ms between updates) and
        // minor floating-point differences in the engine's computation.
        if (overshoot > 1.20) {
            double buffer = data.addBuffer("pred_b_buf", 1);
            if (buffer > 5) {
                flag(player, data, 2.0, "speed=" + String.format("%.4f", actualVXZ)
                        + " max=" + String.format("%.4f", maxVXZ)
                        + " ratio=" + String.format("%.2f", overshoot)
                        + " sneak=" + data.isSneaking()
                        + " sprint=" + data.isSprinting());
                data.setBuffer("pred_b_buf", 2);
            }
        } else {
            data.decreaseBuffer("pred_b_buf", 0.5);
        }
    }
}
