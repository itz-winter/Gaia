package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Aim (Q) - Detects extremely sharp reversals in aim direction.
 */
public class AimQ extends Check {
    public AimQ(GaiaPlugin plugin) { super(plugin, "Aim", "Q", "aim", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        float yaw = data.getYaw();
        float lastYaw = data.getLastYaw();
        float deltaYaw = data.getDeltaYaw();
        float lastDeltaYaw = data.getLastDeltaYaw();

        // Detect sharp direction reversal
        if (deltaYaw > 30.0f && lastDeltaYaw > 30.0f) {
            float yawDir = yaw - lastYaw;
            // Check if large deltas in opposite directions
            if ((yawDir > 0 && data.getBuffer("aim_q_lastDir") < 0)
                    || (yawDir < 0 && data.getBuffer("aim_q_lastDir") > 0)) {
                double buffer = data.addBuffer("aim_q_buffer", 1);
                if (buffer > 6) {
                    flag(player, data, "sharpReversal dYaw=" + deltaYaw + " dir=" + yawDir);
                    data.setBuffer("aim_q_buffer", 0);
                }
            } else {
                data.decreaseBuffer("aim_q_buffer", 0.25);
            }
            data.setBuffer("aim_q_lastDir", yawDir);
        }
    }
}
