package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Scaffold (H) - Detects high placement rate while sprinting. */
public class ScaffoldH extends Check {
    public ScaffoldH(GaiaPlugin plugin) { super(plugin, "Scaffold", "H", "scaffold", true, 8); }
    @Override public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;
        long timeSincePlace = System.currentTimeMillis() - data.getLastBlockPlaceTime();
        if (timeSincePlace < 100 && data.isSprinting() && data.getDeltaXZ() > 0.2) {
            double buffer = data.addBuffer("scaffold_h_buffer", 1);
            if (buffer > 8) {
                flag(player, data, "sprintScaffold speed=" + String.format("%.3f", data.getDeltaXZ()));
                data.setBuffer("scaffold_h_buffer", 0);
            }
        } else { data.decreaseBuffer("scaffold_h_buffer", 0.5); }
    }
}
