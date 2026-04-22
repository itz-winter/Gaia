package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Scaffold (O) - Detects scaffold with abnormal Y delta on placement. */
public class ScaffoldO extends Check {
    public ScaffoldO(GaiaPlugin plugin) { super(plugin, "Scaffold", "O", "scaffold", true, 8); }
    @Override public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data) || data.isFlying() || data.isInVehicle()) return;
        long timeSincePlace = System.currentTimeMillis() - data.getLastActualBlockPlaceTime();
        if (timeSincePlace < 100 && Math.abs(data.getDeltaY()) > 0.5 && data.getDeltaXZ() > 0.1) {
            double buffer = data.addBuffer("scaffold_o_buffer", 1);
            if (buffer > 5) {
                flag(player, data, "yDeltaPlace dY=" + String.format("%.3f", data.getDeltaY()));
                data.setBuffer("scaffold_o_buffer", 0);
            }
        } else { data.decreaseBuffer("scaffold_o_buffer", 0.5); }
    }
}
