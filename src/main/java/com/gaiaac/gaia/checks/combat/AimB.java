package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import com.gaiaac.gaia.util.math.MathUtil;
import org.bukkit.entity.Player;

/**
 * Aim (B) - Detects impossible rotation acceleration.
 * Monitors sudden jumps in rotation speed that exceed human capabilities.
 */
public class AimB extends Check {

    public AimB(GaiaPlugin plugin) {
        super(plugin, "Aim", "B", "aim", true, 10);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        float deltaYaw = data.getDeltaYaw();
        float lastDeltaYaw = data.getLastDeltaYaw();
        float deltaPitch = data.getDeltaPitch();

        if (deltaYaw < 1.0f && deltaPitch < 1.0f) return;

        // Check for impossible acceleration
        float yawAccel = Math.abs(deltaYaw - lastDeltaYaw);

        // Very high acceleration with very small prior movement
        if (yawAccel > 60 && lastDeltaYaw < 0.5f && deltaYaw > 40) {
            double buffer = data.addBuffer("aim_b_buffer", 1);
            if (buffer > 4) {
                flag(player, data, 1.0, "yawAccel=" + yawAccel + " dYaw=" + deltaYaw + " lastDYaw=" + lastDeltaYaw);
                data.setBuffer("aim_b_buffer", 1);
            }
        } else {
            data.decreaseBuffer("aim_b_buffer", 0.5);
        }
    }
}
