package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Scaffold (J) - Detects rotation snapping to exact 90-degree angles during placement. */
public class ScaffoldJ extends Check {
    public ScaffoldJ(GaiaPlugin plugin) { super(plugin, "Scaffold", "J", "scaffold", true, 8); }
    @Override public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data)) return;
        long timeSincePlace = System.currentTimeMillis() - data.getLastBlockPlaceTime();
        if (timeSincePlace < 150) {
            float yaw = Math.abs(data.getYaw() % 90);
            if (yaw < 1.0f || yaw > 89.0f) {
                double buffer = data.addBuffer("scaffold_j_buffer", 1);
                if (buffer > 8) {
                    flag(player, data, "cardinalSnap yaw=" + String.format("%.2f", data.getYaw()));
                    data.setBuffer("scaffold_j_buffer", 0);
                }
            } else { data.decreaseBuffer("scaffold_j_buffer", 0.5); }
        }
    }
}
