package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Velocity (A) - Detects modified knockback (horizontal + vertical).
 * Compares actual velocity response to expected server-sent velocity.
 * Checks both horizontal reduction and vertical cancellation.
 */
public class VelocityA extends Check {

    public VelocityA(GaiaPlugin plugin) {
        super(plugin, "Velocity", "A", "velocity", true, 10);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        if (!data.hasReceivedVelocity() || recentlyTeleported(data)) return;

        long timeSinceVelocity = System.currentTimeMillis() - data.getLastVelocityTime();
        // Check in the window where velocity should be applied (after 1-2 ticks, before friction eats it)
        if (timeSinceVelocity > 600 || timeSinceVelocity < 50) return;

        double expectedH = Math.sqrt(data.getVelocityX() * data.getVelocityX() + data.getVelocityZ() * data.getVelocityZ());
        double actualH = data.getDeltaXZ();
        double expectedV = data.getVelocityY();
        double actualV = data.getDeltaY();

        if (expectedH < 0.1 && expectedV < 0.1) return;

        // === Horizontal velocity check ===
        if (expectedH > 0.1) {
            double hRatio = actualH / expectedH;
            // Compensate for lag — lower threshold with higher ping
            double minRatio = Math.max(0.2, 0.5 - (data.getPing() / 500.0));

            if (hRatio < minRatio) {
                double buffer = data.addBuffer("velocity_a_h_buffer", 1);
                if (buffer > 3) {
                    flag(player, data, 2.0, "hRatio=" + String.format("%.2f", hRatio)
                            + " expectedH=" + String.format("%.2f", expectedH)
                            + " actualH=" + String.format("%.2f", actualH));
                    data.setBuffer("velocity_a_h_buffer", 1);
                }
            } else {
                data.decreaseBuffer("velocity_a_h_buffer", 0.5);
            }
        }

        // === Vertical velocity check (anti-velocity Y cancel) ===
        if (expectedV > 0.2) {
            double vRatio = actualV / expectedV;
            double minVRatio = Math.max(0.1, 0.3 - (data.getPing() / 600.0));

            if (vRatio < minVRatio && actualV < 0.05) {
                double buffer = data.addBuffer("velocity_a_v_buffer", 1);
                if (buffer > 3) {
                    flag(player, data, 2.0, "vRatio=" + String.format("%.2f", vRatio)
                            + " expectedV=" + String.format("%.2f", expectedV)
                            + " actualV=" + String.format("%.2f", actualV));
                    data.setBuffer("velocity_a_v_buffer", 1);
                }
            } else {
                data.decreaseBuffer("velocity_a_v_buffer", 0.5);
            }
        }

        // Reset velocity tracking after analysis window
        if (timeSinceVelocity > 500) {
            data.setHasReceivedVelocity(false);
        }
    }
}
