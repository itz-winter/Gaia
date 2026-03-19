package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * AutoClicker (A) - Detects impossibly high CPS.
 * Human CPS range is typically 6-12 CPS, with butterfly clicking up to ~20.
 */
public class AutoClickerA extends Check {

    private static final int MAX_CPS = 22;

    public AutoClickerA(GaiaPlugin plugin) {
        super(plugin, "AutoClicker", "A", "autoclicker", true, 10);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        int cps = data.getCPS();

        if (cps > MAX_CPS) {
            flag(player, data, 2.0, "highCPS=" + cps + " max=" + MAX_CPS);
        }
    }
}
