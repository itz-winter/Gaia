package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Elytra (B) - Detects elytra vertical boost exploit. */
public class ElytraB extends Check {
    public ElytraB(GaiaPlugin plugin) { super(plugin, "Elytra", "B", "elytra", true, 8); }
    @Override
    public void handle(Player player, PlayerData data) {
        if (!data.isGliding() || recentlyTeleported(data)) return;
        if (data.getDeltaY() > 1.5) {
            flag(player, data, 1.5, "elytraVerticalBoost dY=" + String.format("%.4f", data.getDeltaY()));
        }
    }
}
