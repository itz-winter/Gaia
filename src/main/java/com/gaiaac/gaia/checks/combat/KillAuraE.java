package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import com.gaiaac.gaia.util.math.MathUtil;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * KillAura (E) - Detects consistent attack timing patterns.
 * Human click intervals vary significantly. Bots have very low standard deviation.
 */
public class KillAuraE extends Check {
    public KillAuraE(GaiaPlugin plugin) { super(plugin, "KillAura", "E", "killaura", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data)) return;

        List<Long> clicks = data.getClickTimestamps();
        if (clicks.size() < 10) return;

        // Calculate intervals between last 10 clicks
        int count = Math.min(clicks.size(), 10);
        double[] intervals = new double[count - 1];
        synchronized (clicks) {
            int start = clicks.size() - count;
            for (int i = 0; i < intervals.length; i++) {
                intervals[i] = clicks.get(start + i + 1) - clicks.get(start + i);
            }
        }

        double stdDev = MathUtil.standardDeviation(intervals);
        double mean = MathUtil.mean(intervals);

        // Very consistent intervals (low stddev) at high CPS is suspicious
        if (stdDev < 3.0 && mean < 120 && mean > 10 && data.getCPS() > 8) {
            double buffer = data.addBuffer("killaura_e_buffer", 1);
            if (buffer > 4) {
                flag(player, data, 1.5, "consistentTiming stdDev=" + String.format("%.2f", stdDev)
                        + " mean=" + String.format("%.1f", mean) + "ms cps=" + data.getCPS());
                data.setBuffer("killaura_e_buffer", 1);
            }
        } else {
            data.decreaseBuffer("killaura_e_buffer", 0.5);
        }
    }
}
