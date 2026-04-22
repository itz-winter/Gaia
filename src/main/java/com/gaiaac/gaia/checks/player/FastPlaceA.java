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
        // Use lastActualBlockPlaceTime (confirmed by BlockPlaceEvent) so that right-clicking containers,
        // doors, or other interactive blocks doesn't count as a "placement" and cause false positives.
        long lastPlace = data.getLastActualBlockPlaceTime();
        if (lastPlace > 0) {
            long interval = now - lastPlace;
            // Less than 2 ticks (100ms) between placements — normal bridging is ~150ms+
            if (interval < 100 && interval >= 0) {
                double buffer = data.addBuffer("fastplace_a_buffer", 1);
                if (buffer > 8) {
                    flag(player, data, 1.0, "fastPlace interval=" + interval + "ms");
                    data.setBuffer("fastplace_a_buffer", 0);
                }
            } else {
                data.decreaseBuffer("fastplace_a_buffer", 1.0);
            }
        }
    }
}
