package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import com.gaiaac.gaia.util.math.MathUtil;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * AutoClicker (D) - Detects jitter pattern anomalies in click distribution.
 * High CPS with very low kurtosis suggests a mechanical/automated clicking pattern.
 * Real jitter clicking has high variance but specific distribution shapes.
 */
public class AutoClickerD extends Check {
    public AutoClickerD(GaiaPlugin plugin) { super(plugin, "AutoClicker", "D", "autoclicker", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        List<Long> clicks = data.getClickTimestamps();
        if (clicks.size() < 12) return;

        int cps = data.getCPS();
        if (cps < 10) return; // Only check at high CPS

        // Calculate intervals
        int count = Math.min(clicks.size(), 12);
        double[] intervals = new double[count - 1];
        synchronized (clicks) {
            int start = clicks.size() - count;
            for (int i = 0; i < intervals.length; i++) {
                intervals[i] = clicks.get(start + i + 1) - clicks.get(start + i);
            }
        }

        double kurtosis = MathUtil.kurtosis(intervals);
        double skewness = MathUtil.skewness(intervals);
        int duplicates = MathUtil.duplicates(intervals);

        // Very negative kurtosis (flat distribution) at high CPS = likely autoclicker
        // Many duplicate intervals = very robotic pattern
        if ((kurtosis < -1.0 && cps > 14) || (duplicates > 6 && cps > 12)) {
            double buffer = data.addBuffer("autoclick_d_buffer", 1);
            if (buffer > 4) {
                flag(player, data, 1.5, "jitter kurtosis=" + String.format("%.2f", kurtosis)
                        + " skewness=" + String.format("%.2f", skewness)
                        + " dupes=" + duplicates + " cps=" + cps);
                data.setBuffer("autoclick_d_buffer", 1);
            }
        } else {
            data.decreaseBuffer("autoclick_d_buffer", 0.5);
        }
    }
}
