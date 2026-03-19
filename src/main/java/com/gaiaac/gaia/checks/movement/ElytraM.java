package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Elytra (M) - Detects elytra with modified vertical velocity (ignoring gravity). */
public class ElytraM extends Check {
    public ElytraM(GaiaPlugin plugin) { super(plugin, "Elytra", "M", "elytra", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (!data.isGliding() || recentlyTeleported(data) || recentlyReceivedVelocity(data)) return;

        double deltaY = data.getDeltaY();
        float pitch = data.getPitch();

        // When gliding level or slightly upward, deltaY should still have gravity component
        if (pitch > -20 && pitch < 20 && deltaY > 0.1) {
            double buffer = data.addBuffer("elytra_m_buffer", 1);
            if (buffer > 8) {
                flag(player, data, "noGravity dY=" + String.format("%.4f", deltaY) + " pitch=" + pitch);
                data.setBuffer("elytra_m_buffer", 0);
            }
        } else {
            data.decreaseBuffer("elytra_m_buffer", 0.5);
        }
    }
}
