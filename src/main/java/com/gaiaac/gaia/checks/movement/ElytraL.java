package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Elytra (L) - Detects elytra phase through blocks. */
public class ElytraL extends Check {
    public ElytraL(GaiaPlugin plugin) { super(plugin, "Elytra", "L", "elytra", true, 5); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (!data.isGliding() || recentlyTeleported(data)) return;

        // Detect very high speed that could indicate phasing
        double speed3D = Math.sqrt(data.getDeltaX() * data.getDeltaX() + data.getDeltaY() * data.getDeltaY() + data.getDeltaZ() * data.getDeltaZ());
        if (speed3D > 5.0) {
            flag(player, data, "elytraPhase speed3D=" + String.format("%.3f", speed3D));
        }
    }
}
