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
        if (recentlyTeleported(data) || recentlyJoined(data) || data.isGliding() || data.isInVehicle()) return;
        if (recentlyReceivedVelocity(data)) return;

        double deltaY = data.getDeltaY();
        // Terminal velocity in Minecraft is about -3.92 blocks/tick for free fall
        if (deltaY < -4.0) {
            flag(player, data, "terminalVelocity dY=" + String.format("%.3f", deltaY));
        }
    }
}
