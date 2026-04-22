package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/**
 * Scaffold (A) - Detects automated block placement timing.
 * Scaffold cheats have clock-driven placement intervals (often exactly 1 tick = 50ms).
 * Consecutive placements within ±15ms of each other at < 75ms each indicate automation.
 */
public class ScaffoldA extends Check {
    public ScaffoldA(GaiaPlugin plugin) { super(plugin, "Scaffold", "A", "scaffoldtimed", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || data.isFlying() || data.isInVehicle()) return;
        if (isBedrockPlayer(data)) return;

        long now = System.currentTimeMillis();
        long lastPlace = data.getLastBlockPlaceTime();
        if (lastPlace == 0) return;

        long interval = now - lastPlace;
        if (interval <= 0 || interval > 500) {
            // Too long since last placement — reset consistency tracking
            data.setBuffer("scaffold_a_last", 0);
            return;
        }

        double lastInterval = data.getBuffer("scaffold_a_last");
        // Two consecutive intervals both < 75ms and within 15ms of each other = machine-like precision
        if (lastInterval > 0 && interval < 75 && lastInterval < 75 && Math.abs(interval - lastInterval) < 15) {
            double buffer = data.addBuffer("scaffold_a_buf", 1);
            if (buffer > 4) {
                flag(player, data, 1.0, "scaffoldTiming interval=" + interval + "ms lastInterval=" + (long) lastInterval + "ms");
                data.setBuffer("scaffold_a_buf", 2);
            }
        } else {
            data.decreaseBuffer("scaffold_a_buf", 0.5);
        }
        data.setBuffer("scaffold_a_last", interval);
    }
}
