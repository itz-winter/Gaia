package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Elytra (K) - Detects elytra instant acceleration. */
public class ElytraK extends Check {
    public ElytraK(GaiaPlugin plugin) { super(plugin, "Elytra", "K", "elytra", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (!data.isGliding() || recentlyTeleported(data) || recentlyReceivedVelocity(data)) return;

        double speed = data.getDeltaXZ();
        double lastSpeed = data.getBuffer("elytra_k_lastSpeed");

        if (lastSpeed > 0 && speed - lastSpeed > 1.5) {
            flag(player, data, "elytraAccel cur=" + String.format("%.3f", speed) + " last=" + String.format("%.3f", lastSpeed));
        }
        data.setBuffer("elytra_k_lastSpeed", speed);
    }
}
