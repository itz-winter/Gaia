package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Elytra (F) - Detects elytra flight with no vertical change (hover). */
public class ElytraF extends Check {
    public ElytraF(GaiaPlugin plugin) { super(plugin, "Elytra", "F", "elytra", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (!data.isGliding() || recentlyTeleported(data)) return;

        double deltaY = Math.abs(data.getDeltaY());
        if (deltaY < 0.01 && data.getDeltaXZ() > 0.5) {
            double buffer = data.addBuffer("elytra_f_buffer", 1);
            if (buffer > 10) {
                flag(player, data, "elytraHover dY=" + String.format("%.4f", deltaY));
                data.setBuffer("elytra_f_buffer", 0);
            }
        } else {
            data.decreaseBuffer("elytra_f_buffer", 0.5);
        }
    }
}
