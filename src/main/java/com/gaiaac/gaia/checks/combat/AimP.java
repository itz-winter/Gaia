package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Aim (P) - Detects consistent yaw-to-pitch ratio (locked aim ratio).
 */
public class AimP extends Check {
    public AimP(GaiaPlugin plugin) { super(plugin, "Aim", "P", "aim", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        float deltaYaw = data.getDeltaYaw();
        float deltaPitch = data.getDeltaPitch();

        if (deltaPitch > 0.5f && deltaYaw > 0.5f) {
            double ratio = deltaYaw / deltaPitch;
            double lastRatio = data.getBuffer("aim_p_lastRatio");

            if (lastRatio > 0 && Math.abs(ratio - lastRatio) < 0.01 && ratio > 0.1) {
                double buffer = data.addBuffer("aim_p_buffer", 1);
                if (buffer > 12) {
                    flag(player, data, "lockedRatio r=" + String.format("%.4f", ratio));
                    data.setBuffer("aim_p_buffer", 0);
                }
            } else {
                data.decreaseBuffer("aim_p_buffer", 0.5);
            }
            data.setBuffer("aim_p_lastRatio", ratio);
        }
    }
}
