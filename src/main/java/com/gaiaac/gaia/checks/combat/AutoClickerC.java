package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * AutoClicker (C) - Detects impossible click patterns (no doubles/triples).
 * Real clicking produces occasional double-clicks and irregular gaps.
 */
public class AutoClickerC extends Check {

    public AutoClickerC(GaiaPlugin plugin) {
        super(plugin, "AutoClicker", "C", "autoclicker", true, 10);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        List<Long> clicks = data.getClickTimestamps();
        if (clicks.size() < 15) return;

        int doubleClicks = 0;
        for (int i = 1; i < clicks.size(); i++) {
            long diff = clicks.get(i) - clicks.get(i - 1);
            if (diff < 20) doubleClicks++;
        }

        // Real clicking has occasional double-clicks; perfect autoclickers have none
        int cps = data.getCPS();
        if (doubleClicks == 0 && cps > 10) {
            double buffer = data.addBuffer("autoclick_c_buffer", 1);
            if (buffer > 5) {
                flag(player, data, 1.5, "noDoubleClicks cps=" + cps);
                data.setBuffer("autoclick_c_buffer", 1);
            }
        } else {
            data.decreaseBuffer("autoclick_c_buffer", 0.25);
        }
    }
}
