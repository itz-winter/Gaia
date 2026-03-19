package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** AutoClicker (N) - Detects double-clicking patterns (two clicks per tick). */
public class AutoClickerN extends Check {
    public AutoClickerN(GaiaPlugin plugin) { super(plugin, "AutoClicker", "N", "autoclicker", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        java.util.List<Long> clicks = data.getClickTimestamps();
        if (clicks.size() < 6) return;

        int doubleClicks = 0;
        for (int i = 1; i < clicks.size(); i++) {
            if (clicks.get(i) - clicks.get(i - 1) < 10) {
                doubleClicks++;
            }
        }

        if (doubleClicks > clicks.size() / 3) {
            double buffer = data.addBuffer("ac_n_buffer", 1);
            if (buffer > 5) {
                flag(player, data, "doubleClick count=" + doubleClicks);
                data.setBuffer("ac_n_buffer", 0);
            }
        } else {
            data.decreaseBuffer("ac_n_buffer", 0.5);
        }
    }
}
