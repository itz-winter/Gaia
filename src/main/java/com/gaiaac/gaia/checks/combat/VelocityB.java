package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Velocity (B) - Detects vertical velocity cancellation. */
public class VelocityB extends Check {
    public VelocityB(GaiaPlugin plugin) { super(plugin, "Velocity", "B", "velocity", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (!data.hasReceivedVelocity() || recentlyTeleported(data)) return;
        long timeSinceVelocity = System.currentTimeMillis() - data.getLastVelocityTime();
        if (timeSinceVelocity > 500 || timeSinceVelocity < 50) return;

        double expectedY = data.getVelocityY();
        double actualY = data.getDeltaY();
        if (expectedY < 0.2) return;

        double ratio = actualY / expectedY;
        if (ratio < 0.3) {
            double buffer = data.addBuffer("velocity_b_buffer", 1);
            if (buffer > 3) {
                flag(player, data, 1.5, "vRatio=" + String.format("%.2f", ratio) + " expectedY=" + String.format("%.2f", expectedY));
                data.setBuffer("velocity_b_buffer", 0);
            }
        } else {
            data.decreaseBuffer("velocity_b_buffer", 0.5);
        }
    }
}
