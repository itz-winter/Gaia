package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Scaffold (L) - Detects scaffold at edges with no deceleration. */
public class ScaffoldL extends Check {
    public ScaffoldL(GaiaPlugin plugin) { super(plugin, "Scaffold", "L", "scaffold", true, 10); }
    @Override public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data) || data.isFlying() || data.isInVehicle()) return;
        long timeSincePlace = System.currentTimeMillis() - data.getLastActualBlockPlaceTime();
        double speed = data.getDeltaXZ();
        double lastSpeed = data.getBuffer("scaffold_l_lastSpeed");
        if (timeSincePlace < 150 && speed > 0.2 && lastSpeed > 0 && Math.abs(speed - lastSpeed) < 0.01) {
            double buffer = data.addBuffer("scaffold_l_buffer", 1);
            if (buffer > 10) {
                flag(player, data, "constantSpeed=" + String.format("%.3f", speed));
                data.setBuffer("scaffold_l_buffer", 0);
            }
        } else { data.decreaseBuffer("scaffold_l_buffer", 0.5); }
        data.setBuffer("scaffold_l_lastSpeed", speed);
    }
}
