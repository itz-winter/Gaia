package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Aim (W) - Detects extremely high pitch acceleration.
 */
public class AimW extends Check {
    public AimW(GaiaPlugin plugin) { super(plugin, "Aim", "W", "aim", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        float deltaPitch = data.getDeltaPitch();
        float lastDeltaPitch = data.getLastDeltaPitch();

        if (deltaPitch > 0 && lastDeltaPitch > 0) {
            double pitchAccel = Math.abs(deltaPitch - lastDeltaPitch);
            if (pitchAccel > 60.0 && deltaPitch > 20.0) {
                double buffer = data.addBuffer("aim_w_buffer", 1);
                if (buffer > 5) {
                    flag(player, data, "pitchAccel=" + String.format("%.2f", pitchAccel)
                            + " dPitch=" + deltaPitch);
                    data.setBuffer("aim_w_buffer", 0);
                }
            } else {
                data.decreaseBuffer("aim_w_buffer", 0.25);
            }
        }
    }
}
