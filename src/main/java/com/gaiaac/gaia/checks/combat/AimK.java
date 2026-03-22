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

        // Much stricter: require large deltas AND exact match — small identical deltas are normal mouse
        if (deltaYaw > 3.0f && deltaPitch > 1.0f
                && deltaYaw == lastDeltaYaw && deltaPitch == lastDeltaPitch) {
            double buffer = data.addBuffer("aim_k_buffer", 1);
            if (buffer > 12) {
                flag(player, data, "identicalDelta dYaw=" + deltaYaw + " dPitch=" + deltaPitch);
                data.setBuffer("aim_k_buffer", 4);
            }
        } else {
            data.decreaseBuffer("aim_k_buffer", 1.0);
        }
    }
}
