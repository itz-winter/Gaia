package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Elytra (G) - Detects elytra ascending without fireworks. */
public class ElytraG extends Check {
    public ElytraG(GaiaPlugin plugin) { super(plugin, "Elytra", "G", "elytra", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (!data.isGliding() || recentlyTeleported(data) || recentlyReceivedVelocity(data)) return;

        double deltaY = data.getDeltaY();
        if (deltaY > 0.5) {
            double buffer = data.addBuffer("elytra_g_buffer", 1);
            if (buffer > 6) {
                flag(player, data, "elytraAscend dY=" + String.format("%.3f", deltaY));
                data.setBuffer("elytra_g_buffer", 0);
            }
        } else {
            data.decreaseBuffer("elytra_g_buffer", 0.5);
        }
    }
}
