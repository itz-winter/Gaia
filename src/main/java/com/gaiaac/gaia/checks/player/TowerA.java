package com.gaiaac.gaia.checks.player;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Tower (A) - Detects building upward at impossible speeds.
 * Flags when a player places blocks below themselves and ascends faster than expected.
 */
public class TowerA extends Check {

    public TowerA(GaiaPlugin plugin) {
        super(plugin, "Tower", "A", "player", true, 6);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        if (data.isFlying()) return;
        if (data.isInVehicle()) return;
        if (data.isOnClimbable() || data.isInWater() || data.isInLava()) return;
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        double deltaY = data.getY() - data.getLastY();

        // Only check when player is moving upward (potential towering)
        if (deltaY > 0 && deltaY < 0.6) {
            // Check if player recently placed a block
            long timeSincePlace = System.currentTimeMillis() - data.getLastBlockPlaceTime();

            if (timeSincePlace < 200) { // Placed a block recently
                // Normal towering has specific jump mechanics - ~0.42 Y per jump
                // Scaffold/tower cheats often bypass jump and directly ascend

                double buffer = data.getBuffer("TowerA");

                // If ascending without proper jump arc while placing blocks
                if (deltaY > 0 && deltaY < 0.42 && data.getAirTicks() < 2) {
                    buffer += 1.0;
                    if (buffer > 6) {
                        flag(player, data, String.format("deltaY=%.4f place_interval=%dms", deltaY, timeSincePlace));
                        data.setBuffer("TowerA", buffer / 2);
                    } else {
                        data.setBuffer("TowerA", buffer);
                    }
                } else {
                    data.setBuffer("TowerA", Math.max(0, buffer - 0.5));
                }
            }
        }
    }
}
