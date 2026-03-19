package com.gaiaac.gaia.checks.player;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Invalid (C) - Detects invalid rotation deltas (extremely large yaw/pitch changes).
 */
public class InvalidC extends Check {

    public InvalidC(GaiaPlugin plugin) {
        super(plugin, "Invalid", "C", "player", true, 5);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        float deltaYaw = Math.abs(data.getDeltaYaw());
        float deltaPitch = Math.abs(data.getDeltaPitch());

        // Impossible rotation speed (non-teleport)
        if (deltaYaw > 360.0f || deltaPitch > 180.0f) {
            flag(player, data, String.format("deltaYaw=%.2f deltaPitch=%.2f", deltaYaw, deltaPitch));
        }
    }
}
