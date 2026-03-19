package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Elytra (E) - Detects elytra speed exceeding vanilla maximum. */
public class ElytraE extends Check {
    public ElytraE(GaiaPlugin plugin) { super(plugin, "Elytra", "E", "elytra", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (!data.isGliding() || recentlyTeleported(data)) return;

        double speed = data.getDeltaXZ();
        if (speed > 3.5) {
            flag(player, data, "elytraSpeed=" + String.format("%.3f", speed));
        }
    }
}
