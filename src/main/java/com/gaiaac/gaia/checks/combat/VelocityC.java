package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Velocity (C) - Detects complete velocity cancellation (anti-KB). */
public class VelocityC extends Check {
    public VelocityC(GaiaPlugin plugin) { super(plugin, "Velocity", "C", "velocity", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (!data.hasReceivedVelocity() || recentlyTeleported(data)) return;
        long timeSinceVelocity = System.currentTimeMillis() - data.getLastVelocityTime();
        if (timeSinceVelocity > 200 || timeSinceVelocity < 20) return;

        if (data.getDeltaXZ() < 0.01 && data.getDeltaY() < 0.01 && data.getVelocityY() > 0.1) {
            flag(player, data, 3.0, "zeroDelta expectedY=" + String.format("%.2f", data.getVelocityY()));
            data.setHasReceivedVelocity(false);
        }
    }
}
