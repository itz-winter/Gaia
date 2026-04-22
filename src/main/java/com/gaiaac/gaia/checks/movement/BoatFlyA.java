package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** BoatFly (A) - Detects abnormal boat vertical movement. */
public class BoatFlyA extends Check {
    public BoatFlyA(GaiaPlugin plugin) { super(plugin, "BoatFly", "A", "boatfly", true, 5); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (!data.isInVehicle() || recentlyTeleported(data)) return;
        // Skip for horses/camels — they have a natural jump that can exceed 0.5 dY
        if (data.isRidingJumpableVehicle()) return;
        if (data.getDeltaY() > 0.5 && !data.isInWater()) {
            flag(player, data, 2.0, "boatAscend dY=" + String.format("%.4f", data.getDeltaY()));
        }
    }
}
