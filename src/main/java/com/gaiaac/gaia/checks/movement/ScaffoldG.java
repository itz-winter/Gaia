package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Scaffold (G) - Detects consistent downward pitch during placement (look-down snap). */
public class ScaffoldG extends Check {
    public ScaffoldG(GaiaPlugin plugin) { super(plugin, "Scaffold", "G", "scaffold", true, 8); }
    @Override public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data)) return;
        long timeSincePlace = System.currentTimeMillis() - data.getLastBlockPlaceTime();
        if (timeSincePlace < 150) {
            float pitch = data.getPitch();
            if (pitch > 75.0f && pitch < 90.1f) {
                double buffer = data.addBuffer("scaffold_g_buffer", 1);
                if (buffer > 10) {
                    flag(player, data, "snapDownPitch=" + String.format("%.1f", pitch));
                    data.setBuffer("scaffold_g_buffer", 0);
                }
            } else { data.decreaseBuffer("scaffold_g_buffer", 0.5); }
        }
    }
}
