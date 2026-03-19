package com.gaiaac.gaia.checks.player;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Invalid (B) - Detects invalid position packets (NaN, Infinity, extremely large values).
 */
public class InvalidB extends Check {

    public InvalidB(GaiaPlugin plugin) {
        super(plugin, "Invalid", "B", "player", true, 3);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        double x = data.getX();
        double y = data.getY();
        double z = data.getZ();

        if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)
                || Double.isInfinite(x) || Double.isInfinite(y) || Double.isInfinite(z)) {
            flag(player, data, "NaN/Infinity position");
            return;
        }

        // Extremely large coordinate values
        double maxCoord = 3.0E7; // MC world border
        if (Math.abs(x) > maxCoord || Math.abs(z) > maxCoord || Math.abs(y) > 1000) {
            flag(player, data, String.format("extreme_pos x=%.1f y=%.1f z=%.1f", x, y, z));
        }
    }
}
