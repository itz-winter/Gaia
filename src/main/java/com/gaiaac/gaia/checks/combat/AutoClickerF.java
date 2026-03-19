package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * AutoClicker (F) - Detects impossibly high CPS (above human capability).
 */
public class AutoClickerF extends Check {
    public AutoClickerF(GaiaPlugin plugin) { super(plugin, "AutoClicker", "F", "autoclicker", true, 5); }

    @Override
    public void handle(Player player, PlayerData data) {
        int cps = data.getCPS();
        if (cps > 22) {
            flag(player, data, 2.0, "highCPS=" + cps);
        }
    }
}
