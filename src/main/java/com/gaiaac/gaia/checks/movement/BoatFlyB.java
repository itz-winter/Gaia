package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** BoatFly (B) - Detects boat hover (no descent). */
public class BoatFlyB extends Check {
    public BoatFlyB(GaiaPlugin plugin) { super(plugin, "BoatFly", "B", "boatfly", true, 5); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (!data.isInVehicle() || recentlyTeleported(data)) return;
        if (Math.abs(data.getDeltaY()) < 0.001 && data.getAirTicks() > 20 && !data.isOnGround()) {
            flag(player, data, 1.5, "boatHover airTicks=" + data.getAirTicks());
        }
    }
}
