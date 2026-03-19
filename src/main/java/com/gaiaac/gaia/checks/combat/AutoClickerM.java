package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * AutoClicker (M) - Detects GCD pattern in click intervals (timer-based clickers).
 */
public class AutoClickerM extends Check {
    public AutoClickerM(GaiaPlugin plugin) { super(plugin, "AutoClicker", "M", "autoclicker", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        List<Long> clicks = data.getClickTimestamps();
        if (clicks.size() < 8) return;

        long[] intervals = new long[clicks.size() - 1];
        for (int i = 1; i < clicks.size(); i++) {
            intervals[i - 1] = clicks.get(i) - clicks.get(i - 1);
        }

        // Find GCD of intervals
        long gcd = intervals[0];
        for (int i = 1; i < intervals.length; i++) {
            gcd = com.gaiaac.gaia.util.math.MathUtil.gcd(gcd, intervals[i]);
        }

        // Timer-based clickers have a clear common divisor (e.g., 50ms = 1 tick)
        if (gcd >= 40 && gcd <= 55 && data.getCPS() > 10) {
            double buffer = data.addBuffer("ac_m_buffer", 1);
            if (buffer > 6) {
                flag(player, data, "timerGCD gcd=" + gcd + "ms");
                data.setBuffer("ac_m_buffer", 0);
            }
        } else {
            data.decreaseBuffer("ac_m_buffer", 0.5);
        }
    }
}
