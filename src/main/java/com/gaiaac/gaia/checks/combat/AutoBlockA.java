package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * AutoBlock (A) - Detects blocking while attacking (impossible timing).
 * In vanilla, you cannot block and attack on the exact same tick.
 * Uses packet-tracked item use state instead of Bukkit API for thread safety.
 */
public class AutoBlockA extends Check {

    public AutoBlockA(GaiaPlugin plugin) {
        super(plugin, "AutoBlock", "A", "autoblock", true, 8);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data)) return;

        long attackTime = data.getLastAttackTime();
        long now = System.currentTimeMillis();

        // Check if player was using an item (blocking/eating) within 50ms of an attack
        // This is physically impossible — you must release block before attacking
        if (data.isUsingItem() && now - attackTime < 50) {
            long itemUseTime = data.getLastItemUseTime();
            // Both item use and attack happened within a tiny window
            if (Math.abs(itemUseTime - attackTime) < 100) {
                double buffer = data.addBuffer("autoblock_a_buffer", 1);
                if (buffer > 3) {
                    flag(player, data, 1.5, "blockWhileAttack timeDiff=" + (now - attackTime) + "ms");
                    data.setBuffer("autoblock_a_buffer", 1);
                }
            }
        } else {
            data.decreaseBuffer("autoblock_a_buffer", 0.25);
        }
    }
}
