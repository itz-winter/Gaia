package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** BoatFly (C) - Detects boat speed exploit. */
public class BoatFlyC extends Check {
    public BoatFlyC(GaiaPlugin plugin) { super(plugin, "BoatFly", "C", "boatfly", true, 5); }
    @Override
    public void handle(Player player, PlayerData data) {
        if (!data.isInVehicle() || recentlyTeleported(data)) return;
        if (data.getDeltaXZ() > 2.0) {
            flag(player, data, 1.5, "boatSpeed dXZ=" + String.format("%.4f", data.getDeltaXZ()));
        }
    }
}
