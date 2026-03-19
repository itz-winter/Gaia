package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Criticals (A) - Detects fake airborne packets for critical hits.
 * Players send ground-spoof packets to trigger critical hit damage.
 */
public class CriticalsA extends Check {

    public CriticalsA(GaiaPlugin plugin) {
        super(plugin, "Criticals", "A", "criticals", true, 5);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyReceivedVelocity(data)) return;

        long attackTime = data.getLastAttackTime();
        long now = System.currentTimeMillis();

        if (now - attackTime > 100) return; // Only check near attack time

        // Player claims to not be on ground but has minimal Y movement
        if (!data.isOnGround() && Math.abs(data.getDeltaY()) < 0.05 && data.wasOnGround()) {
            double buffer = data.addBuffer("criticals_a_buffer", 1);
            if (buffer > 2) {
                flag(player, data, 2.0, "fakeAirborne dY=" + data.getDeltaY() + " onGround=" + data.isOnGround());
                data.setBuffer("criticals_a_buffer", 0.5);
            }
        } else {
            data.decreaseBuffer("criticals_a_buffer", 0.25);
        }
    }
}
