package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Aim (H) - Detects micro-rotation inconsistencies (sensitivity mismatch).
 */
public class AimH extends Check {

    public AimH(GaiaPlugin plugin) {
        super(plugin, "Aim", "H", "aim", true, 12);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        float deltaYaw = data.getDeltaYaw();
        float deltaPitch = data.getDeltaPitch();

        // Check for very small but non-zero rotations that don't match any valid sensitivity
        if (deltaYaw > 0 && deltaYaw < 0.01f && deltaPitch > 0 && deltaPitch < 0.01f) {
            double buffer = data.addBuffer("aim_h_buffer", 1);
            if (buffer > 15) {
                flag(player, data, 1.0, "microRotation dYaw=" + deltaYaw + " dPitch=" + deltaPitch);
                data.setBuffer("aim_h_buffer", 0);
            }
        } else {
            data.decreaseBuffer("aim_h_buffer", 0.5);
        }
    }
}
