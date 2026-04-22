package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Motion (E) - Detects vertical speed exceeding terminal velocity. */
public class MotionE extends Check {
    public MotionE(GaiaPlugin plugin) { super(plugin, "Motion", "E", "motion", true, 5); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data) || data.isGliding() || data.isWearingElytra() || data.isInVehicle()) return;
        if (recentlyReceivedVelocity(data)) return;
        if (data.hasLevitation() || data.hasSlowFalling() || data.isRiptiding() || data.isInBubbleColumn()) return;
        if (data.isInWater() || data.isInLava()) return;

        double deltaY = data.getDeltaY();
        // Terminal velocity = gravity / (1 - drag_factor). Default: 0.08 / 0.02 = 4.0 blocks/tick
        // Using the cached GENERIC_GRAVITY attribute so modified gravity (/attribute, datapacks) is respected
        double terminalVelocity = data.getGravityAttribute() / 0.02;
        if (deltaY < -(terminalVelocity + 0.1)) { // small tolerance for floating point / lag
            flag(player, data, "terminalVelocity dY=" + String.format("%.3f", deltaY)
                    + " max=-" + String.format("%.3f", terminalVelocity));
        }
    }
}
