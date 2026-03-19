package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * AutoClicker (I) - Detects many duplicate click intervals (exact timing repetition).
 */
public class AutoClickerI extends Check {
    public AutoClickerI(GaiaPlugin plugin) { super(plugin, "AutoClicker", "I", "autoclicker", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        List<Long> clicks = data.getClickTimestamps();
        if (clicks.size() < 10) return;

        double[] intervals = new double[clicks.size() - 1];
        for (int i = 1; i < clicks.size(); i++) {
            intervals[i - 1] = clicks.get(i) - clicks.get(i - 1);
        }

        int dupes = com.gaiaac.gaia.util.math.MathUtil.duplicates(intervals);
        double dupeRatio = (double) dupes / intervals.length;

        if (dupeRatio > 0.6 && intervals.length >= 8) {
            double buffer = data.addBuffer("ac_i_buffer", 1);
            if (buffer > 5) {
                flag(player, data, "dupIntervals ratio=" + String.format("%.2f", dupeRatio));
                data.setBuffer("ac_i_buffer", 0);
            }
        } else {
            data.decreaseBuffer("ac_i_buffer", 0.5);
        }
    }
}
