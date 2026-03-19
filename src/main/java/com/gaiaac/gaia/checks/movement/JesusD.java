package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Jesus (D) - Detects walking on water at abnormal speed. */
public class JesusD extends Check {
    public JesusD(GaiaPlugin plugin) { super(plugin, "Jesus", "D", "jesus", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data) || data.isInVehicle()) return;

        if (data.isInWater() && data.getDeltaY() >= 0 && !data.isOnGround() && data.getDeltaXZ() > 0.3) {
            double buffer = data.addBuffer("jesus_d_buffer", 1);
            if (buffer > 8) {
                flag(player, data, "waterSpeed dXZ=" + String.format("%.3f", data.getDeltaXZ())
                        + " dY=" + String.format("%.3f", data.getDeltaY()));
                data.setBuffer("jesus_d_buffer", 0);
            }
        } else {
            data.decreaseBuffer("jesus_d_buffer", 0.5);
        }
    }
}
