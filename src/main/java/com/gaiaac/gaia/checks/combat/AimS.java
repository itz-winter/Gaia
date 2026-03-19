package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Aim (S) - Detects step-function yaw changes (discrete snapping in large increments).
 */
public class AimS extends Check {
    public AimS(GaiaPlugin plugin) { super(plugin, "Aim", "S", "aim", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        float deltaYaw = data.getDeltaYaw();

        // Detect snapping to exact degree increments (e.g., 5, 10, 15, 45, 90)
        if (deltaYaw >= 5.0f && deltaYaw % 5.0f < 0.01f) {
            double buffer = data.addBuffer("aim_s_buffer", 1);
            if (buffer > 10) {
                flag(player, data, "stepSnap dYaw=" + deltaYaw);
                data.setBuffer("aim_s_buffer", 0);
            }
        } else {
            data.decreaseBuffer("aim_s_buffer", 0.5);
        }
    }
}
