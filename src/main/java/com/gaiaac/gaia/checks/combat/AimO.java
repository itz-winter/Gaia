package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Aim (O) - Detects extremely high rotation speed (spin aura / fast aim).
 */
public class AimO extends Check {
    public AimO(GaiaPlugin plugin) { super(plugin, "Aim", "O", "aim", true, 6); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        float deltaYaw = data.getDeltaYaw();
        float deltaPitch = data.getDeltaPitch();

        // With proper yaw wrapping, max legitimate delta is ~180°.
        // Only flag absolutely impossible values or sustained insane rotation speed.
        if (deltaYaw > 170.0f && deltaPitch > 30.0f) {
            double buffer = data.addBuffer("aim_o_buffer", 1);
            if (buffer > 3) {
                flag(player, data, "spinAim dYaw=" + String.format("%.2f", deltaYaw)
                        + " dPitch=" + String.format("%.2f", deltaPitch));
                data.setBuffer("aim_o_buffer", 1);
            }
        } else {
            data.decreaseBuffer("aim_o_buffer", 0.5);
        }
    }
}
