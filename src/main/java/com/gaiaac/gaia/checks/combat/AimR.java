package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Aim (R) - Detects aim that always targets head level (pitch consistency).
 */
public class AimR extends Check {
    public AimR(GaiaPlugin plugin) { super(plugin, "Aim", "R", "aim", true, 12); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        float pitch = data.getPitch();
        float deltaPitch = data.getDeltaPitch();

        // Head-level targeting: pitch tends to hover around a very consistent value
        if (pitch > -30 && pitch < 30 && deltaPitch < 0.5f && deltaPitch > 0) {
            double buffer = data.addBuffer("aim_r_buffer", 1);
            if (buffer > 20) {
                flag(player, data, "headLock pitch=" + String.format("%.2f", pitch)
                        + " dPitch=" + String.format("%.3f", deltaPitch));
                data.setBuffer("aim_r_buffer", 0);
            }
        } else {
            data.decreaseBuffer("aim_r_buffer", 1.0);
        }
    }
}
