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
        if (deltaYaw > 1.0f && deltaYaw % 1.0f == 0.0f) {
            double buffer = data.addBuffer("aim_n_buffer", 1);
            if (buffer > 15) {
                flag(player, data, "roundYaw dYaw=" + deltaYaw);
                data.setBuffer("aim_n_buffer", 0);
            }
        } else {
            data.decreaseBuffer("aim_n_buffer", 0.5);
        }
    }
}
