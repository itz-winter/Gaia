package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * AutoClicker (K) - Detects negative skewness in click distribution (left-skewed timing).
 */
public class AutoClickerK extends Check {
    public AutoClickerK(GaiaPlugin plugin) { super(plugin, "AutoClicker", "K", "autoclicker", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        List<Long> clicks = data.getClickTimestamps();
        if (clicks.size() < 12) return;

        double[] intervals = new double[clicks.size() - 1];
        for (int i = 1; i < clicks.size(); i++) {
            intervals[i - 1] = clicks.get(i) - clicks.get(i - 1);
        }

        double skewness = com.gaiaac.gaia.util.math.MathUtil.skewness(intervals);
        if (Math.abs(skewness) < 0.1 && data.getCPS() > 10) {
            double buffer = data.addBuffer("ac_k_buffer", 1);
            if (buffer > 6) {
                flag(player, data, "flatDistribution skew=" + String.format("%.3f", skewness));
                data.setBuffer("ac_k_buffer", 0);
            }
        } else {
            data.decreaseBuffer("ac_k_buffer", 0.5);
        }
    }
}
