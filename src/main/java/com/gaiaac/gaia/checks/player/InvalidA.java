package com.gaiaac.gaia.checks.player;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Invalid (A) - Detects invalid pitch values outside [-90, 90].
 */
public class InvalidA extends Check {

    public InvalidA(GaiaPlugin plugin) {
        super(plugin, "Invalid", "A", "player", true, 3);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        float pitch = data.getPitch();

        if (pitch > 90.0f || pitch < -90.0f) {
            flag(player, data, String.format("pitch=%.2f", pitch));
        }
    }
}
