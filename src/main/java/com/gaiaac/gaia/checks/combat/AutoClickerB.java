package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * AutoClicker (B) - Detects consistent click intervals (low variance).
 * Real human clicks have natural variance; autoclickers produce uniform intervals.
 */
public class AutoClickerB extends Check {

    public AutoClickerB(GaiaPlugin plugin) {
        super(plugin, "AutoClicker", "B", "autoclicker", true, 10);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        List<Long> clicks = data.getClickTimestamps();
        if (clicks.size() < 10) return;

        // Calculate interval variance
        long[] intervals = new long[clicks.size() - 1];
        for (int i = 1; i < clicks.size(); i++) {
            intervals[i - 1] = clicks.get(i) - clicks.get(i - 1);
        }

        double mean = 0;
        for (long interval : intervals) mean += interval;
        mean /= intervals.length;

        double variance = 0;
        for (long interval : intervals) {
            variance += Math.pow(interval - mean, 2);
        }
        variance /= intervals.length;

        double stdDev = Math.sqrt(variance);

        // Extremely low standard deviation indicates autoclicker
        if (stdDev < 5.0 && data.getCPS() > 8) {
            double buffer = data.addBuffer("autoclick_b_buffer", 1);
            if (buffer > 3) {
                flag(player, data, 1.5, "lowVariance stdDev=" + String.format("%.2f", stdDev) + " cps=" + data.getCPS());
                data.setBuffer("autoclick_b_buffer", 1);
            }
        } else {
            data.decreaseBuffer("autoclick_b_buffer", 0.25);
        }
    }
}
