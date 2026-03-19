package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * AutoClicker (G) - Detects extremely low click interval variance.
 */
public class AutoClickerG extends Check {
    public AutoClickerG(GaiaPlugin plugin) { super(plugin, "AutoClicker", "G", "autoclicker", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        List<Long> clicks = data.getClickTimestamps();
        if (clicks.size() < 10) return;

        long[] intervals = new long[clicks.size() - 1];
        for (int i = 1; i < clicks.size(); i++) {
            intervals[i - 1] = clicks.get(i) - clicks.get(i - 1);
        }

        double[] dIntervals = new double[intervals.length];
        for (int i = 0; i < intervals.length; i++) dIntervals[i] = intervals[i];

        double std = com.gaiaac.gaia.util.math.MathUtil.standardDeviation(dIntervals);
        double mean = com.gaiaac.gaia.util.math.MathUtil.mean(dIntervals);

        if (std < 5.0 && mean < 120 && mean > 20) {
            double buffer = data.addBuffer("ac_g_buffer", 1);
            if (buffer > 5) {
                flag(player, data, "lowVariance std=" + String.format("%.2f", std) + " mean=" + String.format("%.1f", mean));
                data.setBuffer("ac_g_buffer", 0);
            }
        } else {
            data.decreaseBuffer("ac_g_buffer", 0.5);
        }
    }
}
