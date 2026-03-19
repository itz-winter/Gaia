package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** FastPlace (A) - Detects abnormal block placement rate. */
public class FastPlaceA extends Check {
    public FastPlaceA(GaiaPlugin plugin) { super(plugin, "FastPlace", "A", "fastplace", true, 8); }
    @Override
    public void handle(Player player, PlayerData data) {
        long now = System.currentTimeMillis();
        long lastPlace = data.getLastBlockPlaceTime();
        if (lastPlace > 0 && now - lastPlace < 50) { // Less than 1 tick between placements
            double buffer = data.addBuffer("fastplace_a_buffer", 1);
            if (buffer > 5) {
                flag(player, data, 1.0, "fastPlace interval=" + (now - lastPlace) + "ms");
                data.setBuffer("fastplace_a_buffer", 0);
            }
        } else {
            data.decreaseBuffer("fastplace_a_buffer", 0.5);
        }
    }
}
