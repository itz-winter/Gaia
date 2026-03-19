package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * AutoClicker (H) - Detects high kurtosis in click intervals (bot-like consistency).
 */
public class AutoClickerH extends Check {
    public AutoClickerH(GaiaPlugin plugin) { super(plugin, "AutoClicker", "H", "autoclicker", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        List<Long> clicks = data.getClickTimestamps();
        if (clicks.size() < 15) return;

        double[] intervals = new double[clicks.size() - 1];
        for (int i = 1; i < clicks.size(); i++) {
            intervals[i - 1] = clicks.get(i) - clicks.get(i - 1);
        }

        double kurtosis = com.gaiaac.gaia.util.math.MathUtil.kurtosis(intervals);
        if (kurtosis > 5.0) {
            double buffer = data.addBuffer("ac_h_buffer", 1);
            if (buffer > 5) {
                flag(player, data, "highKurtosis k=" + String.format("%.2f", kurtosis));
                data.setBuffer("ac_h_buffer", 0);
            }
        } else {
            data.decreaseBuffer("ac_h_buffer", 0.5);
        }
    }
}
