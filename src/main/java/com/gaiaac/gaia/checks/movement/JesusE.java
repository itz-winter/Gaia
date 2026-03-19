package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Jesus (E) - Detects jumping on liquid surfaces (dolphin-like). */
public class JesusE extends Check {
    public JesusE(GaiaPlugin plugin) { super(plugin, "Jesus", "E", "jesus", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data) || data.isInVehicle()) return;

        if ((data.isInWater() || data.isInLava()) && data.getDeltaY() > 0.42) {
            double buffer = data.addBuffer("jesus_e_buffer", 1);
            if (buffer > 5) {
                flag(player, data, "liquidJump dY=" + String.format("%.3f", data.getDeltaY()));
                data.setBuffer("jesus_e_buffer", 0);
            }
        } else {
            data.decreaseBuffer("jesus_e_buffer", 0.5);
        }
    }
}
