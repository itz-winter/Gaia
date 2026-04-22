package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Motion (H) - Detects vertical movement with onGround=true (ground spoof motion). */
public class MotionH extends Check {
    public MotionH(GaiaPlugin plugin) { super(plugin, "Motion", "H", "motion", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data) || data.isInVehicle()) return;
        if (recentlyReceivedVelocity(data) || data.isRiptiding() || data.isInBubbleColumn()) return;
        if (data.isOnSlime()) return; // Slime block bounce can produce large dY while onGround flickers

        if (data.isOnGround() && Math.abs(data.getDeltaY()) > 0.6) {
            double buffer = data.addBuffer("motion_h_buffer", 1);
            if (buffer > 5) {
                flag(player, data, "groundMotion dY=" + String.format("%.3f", data.getDeltaY()));
                data.setBuffer("motion_h_buffer", 0);
            }
        } else {
            data.decreaseBuffer("motion_h_buffer", 0.5);
        }
    }
}
