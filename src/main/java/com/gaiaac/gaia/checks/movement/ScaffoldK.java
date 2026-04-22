package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Scaffold (K) - Detects identical yaw during consecutive placements. */
public class ScaffoldK extends Check {
    public ScaffoldK(GaiaPlugin plugin) { super(plugin, "Scaffold", "K", "scaffold", true, 8); }
    @Override public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || data.isFlying() || data.isInVehicle()) return;
        long timeSincePlace = System.currentTimeMillis() - data.getLastActualBlockPlaceTime();
        if (timeSincePlace < 150 && data.getDeltaYaw() == 0 && data.getDeltaXZ() > 0.1) {
            double buffer = data.addBuffer("scaffold_k_buffer", 1);
            if (buffer > 10) {
                flag(player, data, "staticYawPlace yaw=" + data.getYaw());
                data.setBuffer("scaffold_k_buffer", 0);
            }
        } else { data.decreaseBuffer("scaffold_k_buffer", 0.5); }
    }
}
