package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Aim (U) - Detects oscillating yaw pattern (alternating left-right in equal amounts).
 */
public class AimU extends Check {
    public AimU(GaiaPlugin plugin) { super(plugin, "Aim", "U", "aim", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        float yaw = data.getYaw();
        float lastYaw = data.getLastYaw();
        float deltaYaw = data.getDeltaYaw();

        if (deltaYaw > 3.0f) {
            float direction = yaw - lastYaw;
            double lastDir = data.getBuffer("aim_u_lastDir");

            if (lastDir != 0 && Math.signum(direction) != Math.signum(lastDir)
                    && Math.abs(Math.abs(direction) - Math.abs(lastDir)) < 1.0) {
                double buffer = data.addBuffer("aim_u_buffer", 1);
                if (buffer > 10) {
                    flag(player, data, "oscillate dir=" + String.format("%.2f", direction)
                            + " last=" + String.format("%.2f", lastDir));
                    data.setBuffer("aim_u_buffer", 0);
                }
            } else {
                data.decreaseBuffer("aim_u_buffer", 0.5);
            }
            data.setBuffer("aim_u_lastDir", direction);
        }
    }
}
