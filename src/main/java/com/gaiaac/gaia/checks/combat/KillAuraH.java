package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * KillAura (H) - Detects consistent attack timing (fixed attack interval).
 */
public class KillAuraH extends Check {
    public KillAuraH(GaiaPlugin plugin) { super(plugin, "KillAura", "H", "killaura", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        long now = System.currentTimeMillis();
        long timeSinceAttack = now - data.getLastAttackTime();

        double lastInterval = data.getBuffer("ka_h_lastInterval");
        if (lastInterval > 0 && timeSinceAttack > 0 && timeSinceAttack < 2000) {
            double diff = Math.abs(timeSinceAttack - lastInterval);
            if (diff < 5 && timeSinceAttack < 500) {
                double buffer = data.addBuffer("ka_h_buffer", 1);
                if (buffer > 8) {
                    flag(player, data, "fixedAttackInterval interval=" + timeSinceAttack + " diff=" + diff);
                    data.setBuffer("ka_h_buffer", 0);
                }
            } else {
                data.decreaseBuffer("ka_h_buffer", 0.5);
            }
        }
        data.setBuffer("ka_h_lastInterval", timeSinceAttack);
    }
}
