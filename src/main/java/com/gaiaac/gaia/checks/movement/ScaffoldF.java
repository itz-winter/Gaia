package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Scaffold (F) - Detects placement timing matching exactly one per tick. */
public class ScaffoldF extends Check {
    public ScaffoldF(GaiaPlugin plugin) { super(plugin, "Scaffold", "F", "scaffold", true, 8); }
    @Override public void handle(Player player, PlayerData data) {
        long now = System.currentTimeMillis();
        long timeSincePlace = now - data.getLastBlockPlaceTime();
        double lastInterval = data.getBuffer("scaffold_f_lastInterval");
        if (timeSincePlace > 0 && timeSincePlace < 200 && lastInterval > 0) {
            if (Math.abs(timeSincePlace - lastInterval) < 5 && timeSincePlace < 60) {
                double buffer = data.addBuffer("scaffold_f_buffer", 1);
                if (buffer > 8) {
                    flag(player, data, "tickPlace interval=" + timeSincePlace);
                    data.setBuffer("scaffold_f_buffer", 0);
                }
            } else { data.decreaseBuffer("scaffold_f_buffer", 0.5); }
        }
        data.setBuffer("scaffold_f_lastInterval", timeSincePlace);
    }
}
