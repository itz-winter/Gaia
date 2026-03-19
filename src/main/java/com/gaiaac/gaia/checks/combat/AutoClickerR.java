package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

import java.util.List;

/** AutoClicker (R) - Detects extremely uniform interval distribution (low entropy). */
public class AutoClickerR extends Check {
    public AutoClickerR(GaiaPlugin plugin) { super(plugin, "AutoClicker", "R", "autoclicker", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        List<Long> clicks = data.getClickTimestamps();
        if (clicks.size() < 10) return;

        double[] intervals = new double[clicks.size() - 1];
        for (int i = 1; i < clicks.size(); i++) {
            intervals[i - 1] = clicks.get(i) - clicks.get(i - 1);
        }

        int distinct = com.gaiaac.gaia.util.math.MathUtil.distinctCount(intervals);
        double ratio = (double) distinct / intervals.length;

        if (ratio < 0.3 && data.getCPS() > 8) {
            double buffer = data.addBuffer("ac_r_buffer", 1);
            if (buffer > 6) {
                flag(player, data, "lowEntropy distinct=" + distinct + "/" + intervals.length);
                data.setBuffer("ac_r_buffer", 0);
            }
        } else {
            data.decreaseBuffer("ac_r_buffer", 0.5);
        }
    }
}
