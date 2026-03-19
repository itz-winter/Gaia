package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** AutoClicker (O) - Detects clicking starting immediately on combat engagement. */
public class AutoClickerO extends Check {
    public AutoClickerO(GaiaPlugin plugin) { super(plugin, "AutoClicker", "O", "autoclicker", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        // Detect instant high CPS when combat starts
        long timeSinceAttack = System.currentTimeMillis() - data.getLastAttackTime();
        if (timeSinceAttack < 200 && data.getCPS() > 14) {
            double buffer = data.addBuffer("ac_o_buffer", 1);
            if (buffer > 8) {
                flag(player, data, "instantCombatCPS cps=" + data.getCPS());
                data.setBuffer("ac_o_buffer", 0);
            }
        } else {
            data.decreaseBuffer("ac_o_buffer", 0.25);
        }
    }
}
