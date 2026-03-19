package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Aim (K) - Detects repeated identical yaw/pitch deltas (copy-paste rotations).
 */
public class AimK extends Check {
    public AimK(GaiaPlugin plugin) { super(plugin, "Aim", "K", "aim", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        float deltaYaw = data.getDeltaYaw();
        float deltaPitch = data.getDeltaPitch();
        float lastDeltaYaw = data.getLastDeltaYaw();
        float lastDeltaPitch = data.getLastDeltaPitch();

        if (deltaYaw > 0.5f && deltaPitch > 0.5f
                && deltaYaw == lastDeltaYaw && deltaPitch == lastDeltaPitch) {
            double buffer = data.addBuffer("aim_k_buffer", 1);
            if (buffer > 8) {
                flag(player, data, "identicalDelta dYaw=" + deltaYaw + " dPitch=" + deltaPitch);
                data.setBuffer("aim_k_buffer", 0);
            }
        } else {
            data.decreaseBuffer("aim_k_buffer", 0.5);
        }
    }
}
