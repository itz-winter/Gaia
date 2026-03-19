package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import com.gaiaac.gaia.util.math.MathUtil;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * AutoClicker (E) - Detects perfectly consistent CPS over extended periods.
 * Measures click interval variance over 20 samples. Humans vary significantly.
 * Bots maintain extremely stable intervals even over longer time windows.
 */
public class AutoClickerE extends Check {
    public AutoClickerE(GaiaPlugin plugin) { super(plugin, "AutoClicker", "E", "autoclicker", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        List<Long> clicks = data.getClickTimestamps();
        if (clicks.size() < 16) return;

        int cps = data.getCPS();
        if (cps < 6) return;

        // Use a larger window for extended pattern detection
        int count = Math.min(clicks.size(), 16);
        double[] intervals = new double[count - 1];
        synchronized (clicks) {
            int start = clicks.size() - count;
            for (int i = 0; i < intervals.length; i++) {
                intervals[i] = clicks.get(start + i + 1) - clicks.get(start + i);
            }
        }

        double stdDev = MathUtil.standardDeviation(intervals);
        double mean = MathUtil.mean(intervals);
        double cv = (mean > 0) ? stdDev / mean : 0; // Coefficient of variation

        // Very low coefficient of variation over many samples = bot-like consistency
        // Human CV is typically > 0.15, bots are < 0.05
        if (cv < 0.05 && cps > 8 && mean > 10) {
            double buffer = data.addBuffer("autoclick_e_buffer", 1);
            if (buffer > 5) {
                flag(player, data, 1.5, "consistentCPS cv=" + String.format("%.4f", cv)
                        + " stdDev=" + String.format("%.2f", stdDev)
                        + " mean=" + String.format("%.1f", mean) + "ms cps=" + cps);
                data.setBuffer("autoclick_e_buffer", 2);
            }
        } else {
            data.decreaseBuffer("autoclick_e_buffer", 0.25);
        }
    }
}
