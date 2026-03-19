package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** AutoBlock (C) - Detects impossible sword block timing. */
public class AutoBlockC extends Check {
    public AutoBlockC(GaiaPlugin plugin) { super(plugin, "AutoBlock", "C", "autoblock", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data)) return;
        // Detect unblock -> attack -> reblock within a single tick
        if (player.isBlocking() && System.currentTimeMillis() - data.getLastAttackTime() < 30) {
            double buffer = data.addBuffer("autoblock_c_buffer", 1);
            if (buffer > 5) {
                flag(player, data, 1.0, "instantReblock");
                data.setBuffer("autoblock_c_buffer", 0);
            }
        } else {
            data.decreaseBuffer("autoblock_c_buffer", 0.5);
        }
    }
}
