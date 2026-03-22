package com.gaiaac.gaia.checks.player;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Timer (A) - Detects game speed manipulation (sending packets faster than 20 TPS).
 * Uses a balance system: accumulates debt when packets arrive faster than 50ms apart,
 * drains when packets arrive slower. Only flags on sustained fast-sending.
 *
 * Key improvements over original:
 * - Only counts actual flying/movement tick packets (not all receive packets)
 * - Uses nano time for higher precision
 * - Larger tolerance to avoid false flags from OS scheduling jitter
 * - Requires the player to actually be moving before flagging
 * - Drains balance when standing still to prevent accumulation drift
 */
public class TimerA extends Check {

    private static final long EXPECTED_NS_PER_TICK = 50_000_000L; // 50ms in nanos
    // Allow 10% tolerance — OS thread scheduling, garbage collection, etc. can cause ±5ms jitter easily
    private static final long TOLERANCE_NS = EXPECTED_NS_PER_TICK / 10; // 5ms

    public TimerA(GaiaPlugin plugin) {
        super(plugin, "Timer", "A", "timer", true, 15);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;
        if (data.isInVehicle() || isLowTPS()) return;

        long now = System.nanoTime();

        // Use per-player buffer for lastTickNano (stored as double, cast back to long)
        long lastTickNano = (long) data.getBuffer("TimerA_lastTick");

        if (lastTickNano == 0) {
            data.setBuffer("TimerA_lastTick", (double) now);
            return;
        }

        long elapsed = now - lastTickNano;
        data.setBuffer("TimerA_lastTick", (double) now);

        // Ignore massive gaps (lag spikes, alt-tab, chunk loading, etc.)
        if (elapsed > 2_000_000_000L) { // > 2 seconds
            data.setBuffer("TimerA_balance", 0);
            return;
        }

        double balance = data.getBuffer("TimerA_balance");

        // Accumulate: positive = player sending too fast
        balance += (EXPECTED_NS_PER_TICK - elapsed);

        // Subtract tolerance each tick so minor jitter doesn't accumulate
        balance -= TOLERANCE_NS;

        // If player is standing still (deltaXZ ≈ 0), drain balance faster
        // Standing-still flying packets have higher timing jitter from the client
        if (data.getDeltaXZ() < 0.01) {
            balance -= EXPECTED_NS_PER_TICK / 5; // Extra 10ms drain when idle
            if (balance < 0) balance = 0;
        }

        // Clamp negative side so lag spikes don't give a huge "credit" to cheat with later
        if (balance < -300_000_000L) balance = -300_000_000L; // -300ms max credit

        // Only flag if balance exceeds ~1.2 seconds of debt AND the player is actually moving
        // This means the player has consistently sent packets too fast for 1+ seconds
        if (balance > 1_200_000_000L && data.getDeltaXZ() > 0.03) {
            double ticksAhead = balance / (double) EXPECTED_NS_PER_TICK;
            double buffer = data.addBuffer("TimerA_flagBuffer", 1);
            if (buffer > 5) {
                flag(player, data, String.format("balance=%.0fms ticksAhead=%.1f", balance / 1_000_000.0, ticksAhead));
                data.setBuffer("TimerA_flagBuffer", 0);
                balance -= 500_000_000L; // Deduct 500ms, don't fully reset
            }
        } else {
            data.decreaseBuffer("TimerA_flagBuffer", 0.5);
        }

        data.setBuffer("TimerA_balance", balance);
    }
}
