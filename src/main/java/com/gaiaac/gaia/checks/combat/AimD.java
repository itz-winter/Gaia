package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Aim (D) - Detects perfect tracking / zero-jitter aim.
 * Real humans always have slight jitter in their aim.
 */
public class AimD extends Check {

    public AimD(GaiaPlugin plugin) {
        super(plugin, "Aim", "D", "aim", true, 10);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        float deltaPitch = data.getDeltaPitch();
        float deltaYaw = data.getDeltaYaw();

        if (deltaYaw < 1.0f && deltaPitch < 1.0f) return;

        // Check for impossibly smooth pitch (no micro-adjustments)
        float pitchDiff = Math.abs(deltaPitch - data.getLastDeltaPitch());

        if (pitchDiff == 0 && deltaPitch > 2.0f && deltaYaw > 2.0f) {
            double buffer = data.addBuffer("aim_d_buffer", 1);
            if (buffer > 4) {
                flag(player, data, 1.5, "perfectTracking pitchDiff=0 dPitch=" + deltaPitch + " dYaw=" + deltaYaw);
                data.setBuffer("aim_d_buffer", 1);
            }
        } else {
            data.decreaseBuffer("aim_d_buffer", 0.25);
        }
    }
}
