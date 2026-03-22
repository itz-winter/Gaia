package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Aim (C) - Detects unnatural aim curves / linear rotations.
 * Human aim has natural imprecision; perfectly linear rotations indicate aimbots.
 */
public class AimC extends Check {

    public AimC(GaiaPlugin plugin) {
        super(plugin, "Aim", "C", "aim", true, 12);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        // Only check during combat
        long timeSinceAttack = System.currentTimeMillis() - data.getLastAttackTime();
        if (timeSinceAttack > 2000) return;

        float deltaYaw = data.getDeltaYaw();
        float deltaPitch = data.getDeltaPitch();

        if (deltaYaw < 3.0f || deltaPitch < 3.0f) return;

        // Check for constant deltas (linear aim)
        float lastDeltaYaw = data.getLastDeltaYaw();
        float lastDeltaPitch = data.getLastDeltaPitch();

        float yawDiff = Math.abs(deltaYaw - lastDeltaYaw);
        float pitchDiff = Math.abs(deltaPitch - lastDeltaPitch);

        // Nearly identical consecutive rotations suggest automation
        if (yawDiff < 0.005f && pitchDiff < 0.005f && deltaYaw > 4.0f) {
            double buffer = data.addBuffer("aim_c_buffer", 1);
            if (buffer > 8) {
                flag(player, data, 1.0, "linearAim yawDiff=" + yawDiff + " pitchDiff=" + pitchDiff);
                data.setBuffer("aim_c_buffer", 2);
            }
        } else {
            data.decreaseBuffer("aim_c_buffer", 0.75);
        }
    }
}
