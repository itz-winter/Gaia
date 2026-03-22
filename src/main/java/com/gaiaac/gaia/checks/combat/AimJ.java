package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Aim (J) - Detects zero pitch delta with non-zero yaw (horizontal-only aim lock).
 */
public class AimJ extends Check {
    public AimJ(GaiaPlugin plugin) { super(plugin, "Aim", "J", "aim", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        float deltaYaw = data.getDeltaYaw();
        float deltaPitch = data.getDeltaPitch();

        // Only flag if sustained over many ticks with very high yaw — occasional zero pitch is normal
        if (deltaYaw > 15.0f && deltaPitch == 0.0f && data.getLastDeltaPitch() == 0.0f) {
            double buffer = data.addBuffer("aim_j_buffer", 1);
            if (buffer > 15) {
                flag(player, data, "horizLock dYaw=" + deltaYaw + " dPitch=0");
                data.setBuffer("aim_j_buffer", 5);
            }
        } else {
            data.decreaseBuffer("aim_j_buffer", 1.0);
        }
    }
}
