package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Motion (G) - Detects no vertical deceleration (anti-gravity while falling). */
public class MotionG extends Check {
    public MotionG(GaiaPlugin plugin) { super(plugin, "Motion", "G", "motion", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data) || data.isGliding() || data.isInVehicle()) return;
        if (recentlyReceivedVelocity(data) || data.isOnClimbable() || data.isInWater() || data.isInLava()) return;

        if (!data.isOnGround() && data.getAirTicks() > 3) {
            double deltaY = data.getDeltaY();
            double lastDeltaY = data.getBuffer("motion_g_lastDY");
            // Gravity should cause deltaY to decrease each tick by ~0.08
            if (lastDeltaY != 0 && deltaY < 0 && Math.abs(deltaY - lastDeltaY) < 0.001) {
                double buffer = data.addBuffer("motion_g_buffer", 1);
                if (buffer > 8) {
                    flag(player, data, "flatFall dY=" + String.format("%.4f", deltaY)
                            + " last=" + String.format("%.4f", lastDeltaY));
                    data.setBuffer("motion_g_buffer", 0);
                }
            } else {
                data.decreaseBuffer("motion_g_buffer", 0.5);
            }
            data.setBuffer("motion_g_lastDY", deltaY);
        }
    }
}
