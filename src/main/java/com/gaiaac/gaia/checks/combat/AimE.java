package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Aim (E) - Detects constant pitch/yaw deltas (cinematic aim patterns).
 */
public class AimE extends Check {

    public AimE(GaiaPlugin plugin) {
        super(plugin, "Aim", "E", "aim", true, 10);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        float deltaYaw = data.getDeltaYaw();
        float deltaPitch = data.getDeltaPitch();

        if (deltaYaw < 1.0f || deltaPitch < 1.0f) return;

        // Detect constant yaw/pitch deltas over multiple ticks
        float yawRatio = deltaYaw > 0 ? data.getLastDeltaYaw() / deltaYaw : 0;
        float pitchRatio = deltaPitch > 0 ? data.getLastDeltaPitch() / deltaPitch : 0;

        // If ratio is exactly 1.0, the delta is constant
        if (Math.abs(yawRatio - 1.0) < 0.0005 && Math.abs(pitchRatio - 1.0) < 0.0005 && deltaYaw > 4.0f) {
            double buffer = data.addBuffer("aim_e_buffer", 1);
            if (buffer > 6) {
                flag(player, data, 1.5, "constantDelta yawRatio=" + yawRatio + " pitchRatio=" + pitchRatio);
                data.setBuffer("aim_e_buffer", 2);
            }
        } else {
            data.decreaseBuffer("aim_e_buffer", 0.5);
        }
    }
}
