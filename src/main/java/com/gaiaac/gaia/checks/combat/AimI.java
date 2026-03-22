package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Aim (I) - Detects linear aim interpolation (robotic straight-line tracking).
 */
public class AimI extends Check {
    public AimI(GaiaPlugin plugin) { super(plugin, "Aim", "I", "aim", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        float deltaYaw = data.getDeltaYaw();
        float deltaPitch = data.getDeltaPitch();
        float lastDeltaYaw = data.getLastDeltaYaw();
        float lastDeltaPitch = data.getLastDeltaPitch();

        if (deltaYaw > 1.0f && deltaPitch > 0.5f) {
            double yawRatio = lastDeltaYaw > 0 ? deltaYaw / lastDeltaYaw : 0;
            double pitchRatio = lastDeltaPitch > 0 ? deltaPitch / lastDeltaPitch : 0;

            // Linear interpolation produces very consistent ratios — tighter than human jitter
            if (Math.abs(yawRatio - 1.0) < 0.02 && Math.abs(pitchRatio - 1.0) < 0.02
                    && deltaYaw > 3.0f && deltaPitch > 1.5f) {
                double buffer = data.addBuffer("aim_i_buffer", 1);
                if (buffer > 15) {
                    flag(player, data, "linearInterp yawR=" + String.format("%.3f", yawRatio)
                            + " pitchR=" + String.format("%.3f", pitchRatio));
                    data.setBuffer("aim_i_buffer", 0);
                }
            } else {
                data.decreaseBuffer("aim_i_buffer", 1.0);
            }
        }
    }
}
