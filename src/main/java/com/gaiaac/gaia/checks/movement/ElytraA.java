package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Elytra (A) - Detects elytra boost exploits (speed). */
public class ElytraA extends Check {
    public ElytraA(GaiaPlugin plugin) { super(plugin, "Elytra", "A", "elytra", true, 8); }
    @Override
    public void handle(Player player, PlayerData data) {
        if (!data.isGliding() || recentlyTeleported(data)) return;
        double speed = data.getDeltaXZ();
        if (speed > 3.5) {
            flag(player, data, 1.0, "elytraSpeed=" + String.format("%.4f", speed));
        }
    }
}
