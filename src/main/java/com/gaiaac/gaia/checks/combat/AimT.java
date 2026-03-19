package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Aim (T) - Detects zero-delta rotation followed by instant snap (backtrack aim).
 */
public class AimT extends Check {
    public AimT(GaiaPlugin plugin) { super(plugin, "Aim", "T", "aim", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        float deltaYaw = data.getDeltaYaw();
        float lastDeltaYaw = data.getLastDeltaYaw();

        // Zero rotation followed by large snap
        if (lastDeltaYaw < 0.01f && deltaYaw > 30.0f) {
            double buffer = data.addBuffer("aim_t_buffer", 1);
            if (buffer > 5) {
                flag(player, data, "snapFromIdle last=" + lastDeltaYaw + " cur=" + deltaYaw);
                data.setBuffer("aim_t_buffer", 0);
            }
        } else {
            data.decreaseBuffer("aim_t_buffer", 0.25);
        }
    }
}
