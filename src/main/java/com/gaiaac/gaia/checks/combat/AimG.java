package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Aim (G) - Detects invalid pitch values (beyond -90 to 90 range).
 */
public class AimG extends Check {

    public AimG(GaiaPlugin plugin) {
        super(plugin, "Aim", "G", "aim", true, 5);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        float pitch = data.getPitch();

        if (Math.abs(pitch) > 90.0f) {
            flag(player, data, 5.0, "invalidPitch=" + pitch);
        }
    }
}
