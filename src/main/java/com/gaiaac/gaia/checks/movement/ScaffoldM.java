package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Scaffold (M) - Detects diagonal scaffold bridging. */
public class ScaffoldM extends Check {
    public ScaffoldM(GaiaPlugin plugin) { super(plugin, "Scaffold", "M", "scaffold", true, 10); }
    @Override public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data) || data.isFlying() || data.isInVehicle()) return;
        long timeSincePlace = System.currentTimeMillis() - data.getLastActualBlockPlaceTime();
        if (timeSincePlace < 150) {
            double dx = Math.abs(data.getDeltaX());
            double dz = Math.abs(data.getDeltaZ());
            if (dx > 0.1 && dz > 0.1 && Math.abs(dx - dz) < 0.05) {
                double buffer = data.addBuffer("scaffold_m_buffer", 1);
                if (buffer > 8) {
                    flag(player, data, "diagonalScaffold dx=" + String.format("%.3f", dx) + " dz=" + String.format("%.3f", dz));
                    data.setBuffer("scaffold_m_buffer", 0);
                }
            } else { data.decreaseBuffer("scaffold_m_buffer", 0.5); }
        }
    }
}
