package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Scaffold (I) - Detects placement while airborne (no ground support). */
public class ScaffoldI extends Check {
    public ScaffoldI(GaiaPlugin plugin) { super(plugin, "Scaffold", "I", "scaffold", true, 8); }
    @Override public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data) || data.isFlying() || data.isInVehicle()) return;
        long timeSincePlace = System.currentTimeMillis() - data.getLastActualBlockPlaceTime();
        if (timeSincePlace < 100 && !data.isOnGround() && data.getAirTicks() > 5) {
            double buffer = data.addBuffer("scaffold_i_buffer", 1);
            if (buffer > 5) {
                flag(player, data, "airScaffold airTicks=" + data.getAirTicks());
                data.setBuffer("scaffold_i_buffer", 0);
            }
        } else { data.decreaseBuffer("scaffold_i_buffer", 0.5); }
    }
}
