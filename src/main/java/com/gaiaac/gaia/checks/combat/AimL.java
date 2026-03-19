package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Aim (L) - Detects impossible yaw acceleration (instant direction changes).
 */
public class AimL extends Check {
    public AimL(GaiaPlugin plugin) { super(plugin, "Aim", "L", "aim", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        float deltaYaw = data.getDeltaYaw();
        float lastDeltaYaw = data.getLastDeltaYaw();

        if (lastDeltaYaw > 0 && deltaYaw > 0) {
            double accel = Math.abs(deltaYaw - lastDeltaYaw);
            if (accel > 150.0 && deltaYaw > 30.0 && lastDeltaYaw > 30.0) {
                double buffer = data.addBuffer("aim_l_buffer", 1);
                if (buffer > 5) {
                    flag(player, data, "yawAccel=" + String.format("%.2f", accel)
                            + " dYaw=" + deltaYaw + " last=" + lastDeltaYaw);
                    data.setBuffer("aim_l_buffer", 0);
                }
            } else {
                data.decreaseBuffer("aim_l_buffer", 0.25);
            }
        }
    }
}
