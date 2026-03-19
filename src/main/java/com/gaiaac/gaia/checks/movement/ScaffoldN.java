package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Scaffold (N) - Detects placement with impossible head rotation (headless scaffold). */
public class ScaffoldN extends Check {
    public ScaffoldN(GaiaPlugin plugin) { super(plugin, "Scaffold", "N", "scaffold", true, 8); }
    @Override public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data)) return;
        long timeSincePlace = System.currentTimeMillis() - data.getLastBlockPlaceTime();
        if (timeSincePlace < 100 && data.getPitch() < 0 && data.getDeltaXZ() > 0.15) {
            double buffer = data.addBuffer("scaffold_n_buffer", 1);
            if (buffer > 6) {
                flag(player, data, "headlessPlace pitch=" + String.format("%.1f", data.getPitch()));
                data.setBuffer("scaffold_n_buffer", 0);
            }
        } else { data.decreaseBuffer("scaffold_n_buffer", 0.5); }
    }
}
