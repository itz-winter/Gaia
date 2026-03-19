package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** EntityFlight (A) - Detects airborne entity movement. */
public class EntityFlightA extends Check {
    public EntityFlightA(GaiaPlugin plugin) { super(plugin, "EntityFlight", "A", "entityflight", true, 5); }
    @Override
    public void handle(Player player, PlayerData data) {
        if (!data.isInVehicle() || recentlyTeleported(data)) return;
        if (data.getDeltaY() > 0.5 && data.getAirTicks() > 10 && !data.isInWater()) {
            flag(player, data, 2.0, "entityFly dY=" + String.format("%.4f", data.getDeltaY()) + " airTicks=" + data.getAirTicks());
        }
    }
}
