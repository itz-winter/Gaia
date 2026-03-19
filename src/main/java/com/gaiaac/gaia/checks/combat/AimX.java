package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Aim (X) - Detects smooth aim with no jitter (unnaturally smooth mouse movement).
 */
public class AimX extends Check {
    public AimX(GaiaPlugin plugin) { super(plugin, "Aim", "X", "aim", true, 12); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        float deltaYaw = data.getDeltaYaw();
        float deltaPitch = data.getDeltaPitch();
        float lastDeltaYaw = data.getLastDeltaYaw();
        float lastDeltaPitch = data.getLastDeltaPitch();

        if (deltaYaw > 2.0f && deltaPitch > 1.0f) {
            double yawDiff = Math.abs(deltaYaw - lastDeltaYaw);
            double pitchDiff = Math.abs(deltaPitch - lastDeltaPitch);

            // Unnaturally smooth: very small acceleration with significant movement
            if (yawDiff < 0.05 && pitchDiff < 0.05 && deltaYaw > 5.0f) {
                double buffer = data.addBuffer("aim_x_buffer", 1);
                if (buffer > 15) {
                    flag(player, data, "smoothAim yDiff=" + String.format("%.4f", yawDiff)
                            + " pDiff=" + String.format("%.4f", pitchDiff));
                    data.setBuffer("aim_x_buffer", 0);
                }
            } else {
                data.decreaseBuffer("aim_x_buffer", 0.5);
            }
        }
    }
}
