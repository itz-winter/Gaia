package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Aim (N) - Detects consistent round-number yaw deltas (modulo snap).
 */
public class AimN extends Check {
    public AimN(GaiaPlugin plugin) { super(plugin, "Aim", "N", "aim", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        float deltaYaw = data.getDeltaYaw();
        float deltaPitch = data.getDeltaPitch();
        // Only flag if BOTH yaw AND pitch are round numbers — yaw alone being round is normal
        if (deltaYaw > 2.0f && deltaPitch > 1.0f
                && deltaYaw % 1.0f == 0.0f && deltaPitch % 1.0f == 0.0f) {
            double buffer = data.addBuffer("aim_n_buffer", 1);
            if (buffer > 20) {
                flag(player, data, "roundRotation dYaw=" + deltaYaw + " dPitch=" + deltaPitch);
                data.setBuffer("aim_n_buffer", 8);
            }
        } else {
            data.decreaseBuffer("aim_n_buffer", 1.0);
        }
    }
}
