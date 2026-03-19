package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Elytra (H) - Detects elytra glide with no deceleration over time. */
public class ElytraH extends Check {
    public ElytraH(GaiaPlugin plugin) { super(plugin, "Elytra", "H", "elytra", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (!data.isGliding() || recentlyTeleported(data)) return;

        double speed = data.getDeltaXZ();
        double lastSpeed = data.getBuffer("elytra_h_lastSpeed");

        if (lastSpeed > 0 && speed > 1.0 && Math.abs(speed - lastSpeed) < 0.01) {
            double buffer = data.addBuffer("elytra_h_buffer", 1);
            if (buffer > 12) {
                flag(player, data, "noDecel speed=" + String.format("%.3f", speed));
                data.setBuffer("elytra_h_buffer", 0);
            }
        } else {
            data.decreaseBuffer("elytra_h_buffer", 0.5);
        }
        data.setBuffer("elytra_h_lastSpeed", speed);
    }
}
