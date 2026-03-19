package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * KillAura (I) - Detects attacking while using items (eating/drinking).
 */
public class KillAuraI extends Check {
    public KillAuraI(GaiaPlugin plugin) { super(plugin, "KillAura", "I", "killaura", true, 5); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        long timeSinceAttack = System.currentTimeMillis() - data.getLastAttackTime();
        // Check if player is using an item while attacking
        if (timeSinceAttack < 50 && player.isHandRaised()) {
            double buffer = data.addBuffer("ka_i_buffer", 1);
            if (buffer > 3) {
                flag(player, data, "attackWhileUsing");
                data.setBuffer("ka_i_buffer", 0);
            }
        } else {
            data.decreaseBuffer("ka_i_buffer", 0.25);
        }
    }
}
