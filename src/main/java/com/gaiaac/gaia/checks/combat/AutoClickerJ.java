package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** AutoClicker (J) - Detects impossible click speed sustained over time. */
public class AutoClickerJ extends Check {
    public AutoClickerJ(GaiaPlugin plugin) { super(plugin, "AutoClicker", "J", "autoclicker", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        int cps = data.getCPS();
        if (cps > 16) {
            double buffer = data.addBuffer("ac_j_buffer", 1);
            if (buffer > 8) {
                flag(player, data, "sustainedHighCPS=" + cps);
                data.setBuffer("ac_j_buffer", 0);
            }
        } else {
            data.decreaseBuffer("ac_j_buffer", 1.0);
        }
    }
}
