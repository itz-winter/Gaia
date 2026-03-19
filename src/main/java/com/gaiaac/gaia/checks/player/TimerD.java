package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Timer (D) - Detects packet rate oscillation (alternating fast and slow ticks).
 * Some timer cheats alternate between fast and slow to maintain average TPS
 * while still gaining advantage. Checks coefficient of variation of intervals.
 * Very lenient — normal WiFi jitter causes high CV so threshold is strict.
 */
public class TimerD extends Check {
    public TimerD(GaiaPlugin plugin) { super(plugin, "Timer", "D", "timer", true, 20); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;
        if (data.isInVehicle() || isLowTPS()) return;

        java.util.List<Long> timestamps = data.getPacketTimestamps();
        if (timestamps.size() < 20) return; // Need a bigger sample for meaningful stats

        // Use the last 20 intervals
        int sampleSize = 20;
        double[] intervals = new double[sampleSize];
        int offset = timestamps.size() - sampleSize - 1;
        for (int i = 0; i < sampleSize; i++) {
            intervals[i] = timestamps.get(offset + i + 1) - timestamps.get(offset + i);
        }

        // Filter out any intervals > 500ms (lag spikes), replace with mean
        double rawMean = com.gaiaac.gaia.util.math.MathUtil.mean(intervals);
        for (int i = 0; i < intervals.length; i++) {
            if (intervals[i] > 500) intervals[i] = rawMean;
        }

        double std = com.gaiaac.gaia.util.math.MathUtil.standardDeviation(intervals);
        double mean = com.gaiaac.gaia.util.math.MathUtil.mean(intervals);

        if (mean <= 0) return;

        double cv = std / mean;

        // CV > 1.2 is extremely abnormal even for bad connections once lag spikes are filtered
        // Normal WiFi jitter CV is ~0.3-0.6, bad connections ~0.6-0.9
        if (cv > 1.2 && data.getDeltaXZ() > 0.05) {
            double buffer = data.addBuffer("timer_d_buffer", 1);
            if (buffer > 10) {
                flag(player, data, "timerOscillation cv=" + String.format("%.2f", cv));
                data.setBuffer("timer_d_buffer", 5);
            }
        } else {
            data.decreaseBuffer("timer_d_buffer", 0.5);
        }
    }
}
