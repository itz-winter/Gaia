package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * AutoBlock (B) - Detects attack/block packet overlap patterns.
 */
public class AutoBlockB extends Check {

    public AutoBlockB(GaiaPlugin plugin) {
        super(plugin, "AutoBlock", "B", "autoblock", true, 8);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data)) return;

        long attackTime = data.getLastAttackTime();
        long now = System.currentTimeMillis();

        // Consistent block-attack timing
        if (player.isBlocking() && now - attackTime < 100 && now - attackTime > 0) {
            double buffer = data.addBuffer("autoblock_b_buffer", 1);
            if (buffer > 6) {
                flag(player, data, 1.0, "packetOverlap timeDiff=" + (now - attackTime) + "ms");
                data.setBuffer("autoblock_b_buffer", 0);
            }
        } else {
            data.decreaseBuffer("autoblock_b_buffer", 0.25);
        }
    }
}
