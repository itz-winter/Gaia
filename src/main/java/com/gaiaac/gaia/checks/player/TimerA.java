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
    // Allow 20% tolerance — OS scheduling, GC pauses, network jitter easily cause ±10ms variance
    private static final long TOLERANCE_NS = EXPECTED_NS_PER_TICK / 5; // 10ms

    public TimerA(GaiaPlugin plugin) {
        super(plugin, "Timer", "A", "timer", true, 15);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;
        if (data.isInVehicle() || isLowTPS() || data.isWearingElytra()) return;

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

        // Ignore unrealistically short gaps (duplicate packets, system hiccup)
        if (elapsed < 5_000_000L) { // < 5ms
            return;
        }

        double balance = data.getBuffer("TimerA_balance");

        // Accumulate: positive = player sending too fast
        balance += (EXPECTED_NS_PER_TICK - elapsed);

        // Subtract tolerance each tick so minor jitter doesn't accumulate
        balance -= TOLERANCE_NS;

        // If player is standing still (deltaXZ ≈ 0), drain balance aggressively
        // Standing-still flying packets have very high timing jitter from the client
        if (data.getDeltaXZ() < 0.03) {
            balance -= EXPECTED_NS_PER_TICK; // Full tick drain when idle
            if (balance < 0) balance = 0;
        }

        // Clamp negative side so lag spikes don't give a huge "credit" to cheat with later
        if (balance < -500_000_000L) balance = -500_000_000L; // -500ms max credit

        // Only flag if balance exceeds ~2.5 seconds of debt AND the player is actually moving
        // This means the player has consistently sent packets too fast for 2+ seconds
        if (balance > 2_500_000_000L && data.getDeltaXZ() > 0.08) {
            double ticksAhead = balance / (double) EXPECTED_NS_PER_TICK;
            double buffer = data.addBuffer("TimerA_flagBuffer", 1);
            if (buffer > 8) {
                flag(player, data, String.format("balance=%.0fms ticksAhead=%.1f", balance / 1_000_000.0, ticksAhead));
                data.setBuffer("TimerA_flagBuffer", 0);
                balance -= 1_000_000_000L; // Deduct 1s, don't fully reset
            }
        } else {
            data.decreaseBuffer("TimerA_flagBuffer", 1.0);
        }

        data.setBuffer("TimerA_balance", balance);
    }
}
