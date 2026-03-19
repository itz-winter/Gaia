package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Motion (F) - Detects horizontal speed while in air exceeding limits. */
public class MotionF extends Check {
    public MotionF(GaiaPlugin plugin) { super(plugin, "Motion", "F", "motion", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data) || data.isGliding() || data.isInVehicle()) return;
        if (recentlyReceivedVelocity(data)) return;

        if (!data.isOnGround() && data.getAirTicks() > 5) {
            double deltaXZ = data.getDeltaXZ();
            // Air speed should be decelerating, not constant
            double lastXZ = data.getBuffer("motion_f_lastXZ");
            if (lastXZ > 0 && deltaXZ > 0.3 && deltaXZ >= lastXZ) {
                double buffer = data.addBuffer("motion_f_buffer", 1);
                if (buffer > 6) {
                    flag(player, data, "airAccel dXZ=" + String.format("%.3f", deltaXZ) + " last=" + String.format("%.3f", lastXZ));
                    data.setBuffer("motion_f_buffer", 0);
                }
            } else {
                data.decreaseBuffer("motion_f_buffer", 0.5);
            }
            data.setBuffer("motion_f_lastXZ", deltaXZ);
        }
    }
}
