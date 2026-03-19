package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

import java.util.List;

/** AutoClicker (T) - Detects jitter-click randomization patterns (fake randomness). */
public class AutoClickerT extends Check {
    public AutoClickerT(GaiaPlugin plugin) { super(plugin, "AutoClicker", "T", "autoclicker", true, 12); }

    @Override
    public void handle(Player player, PlayerData data) {
        List<Long> clicks = data.getClickTimestamps();
        if (clicks.size() < 12) return;

        double[] intervals = new double[clicks.size() - 1];
        for (int i = 1; i < clicks.size(); i++) {
            intervals[i - 1] = clicks.get(i) - clicks.get(i - 1);
        }

        double std = com.gaiaac.gaia.util.math.MathUtil.standardDeviation(intervals);
        double mean = com.gaiaac.gaia.util.math.MathUtil.mean(intervals);

        // Fake randomization typically has std in a very specific range relative to mean
        if (mean > 0 && std / mean > 0.15 && std / mean < 0.25 && data.getCPS() > 12) {
            double buffer = data.addBuffer("ac_t_buffer", 1);
            if (buffer > 8) {
                flag(player, data, "fakeJitter cv=" + String.format("%.3f", std / mean) + " cps=" + data.getCPS());
                data.setBuffer("ac_t_buffer", 0);
            }
        } else {
            data.decreaseBuffer("ac_t_buffer", 0.5);
        }
    }
}
