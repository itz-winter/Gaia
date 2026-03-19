package com.gaiaac.gaia.checks.player;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Invalid (D) - Detects invalid movement packets with impossible deltas.
 */
public class InvalidD extends Check {

    public InvalidD(GaiaPlugin plugin) {
        super(plugin, "Invalid", "D", "player", true, 5);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;
        if (data.isFlying()) return;
        if (data.isInVehicle()) return;

        double deltaX = data.getX() - data.getLastX();
        double deltaZ = data.getZ() - data.getLastZ();
        double horizontalDist = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        // No legitimate player can move more than ~10 blocks in a single tick
        if (horizontalDist > 10.0) {
            flag(player, data, String.format("dist=%.2f", horizontalDist));
        }
    }
}
