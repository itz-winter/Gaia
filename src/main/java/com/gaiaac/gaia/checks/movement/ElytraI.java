package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Elytra (I) - Detects starting elytra flight from ground (no jump). */
public class ElytraI extends Check {
    public ElytraI(GaiaPlugin plugin) { super(plugin, "Elytra", "I", "elytra", true, 5); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (!data.isGliding() || recentlyTeleported(data)) return;

        if (data.isOnGround() && data.isGliding()) {
            flag(player, data, "groundElytra");
        }
    }
}
