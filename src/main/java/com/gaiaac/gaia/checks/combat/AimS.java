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
        float deltaPitch = data.getDeltaPitch();

        // Only flag if BOTH yaw and pitch snap to exact 5° increments simultaneously — yaw alone is not suspicious
        if (deltaYaw >= 10.0f && deltaPitch >= 5.0f
                && deltaYaw % 5.0f < 0.001f && deltaPitch % 5.0f < 0.001f) {
            double buffer = data.addBuffer("aim_s_buffer", 1);
            if (buffer > 15) {
                flag(player, data, "stepSnap dYaw=" + deltaYaw + " dPitch=" + deltaPitch);
                data.setBuffer("aim_s_buffer", 5);
            }
        } else {
            data.decreaseBuffer("aim_s_buffer", 1.0);
        }
    }
}
