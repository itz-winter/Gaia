package com.gaiaac.gaia.checks.player;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Timer (A) - Detects game speed manipulation (sending packets faster than 20 TPS).
 * Uses a balance system: accumulates debt when packets arrive faster than 50ms apart,
 * drains when packets arrive slower. Only flags on sustained fast-sending.
 */
public class TimerA extends Check {

    private static final double EXPECTED_MS_PER_TICK = 50.0;
    // Allow 5% speed tolerance to account for system clock drift and minor jitter
    private static final double TOLERANCE_MS = EXPECTED_MS_PER_TICK * 0.05;

    public TimerA(GaiaPlugin plugin) {
        super(plugin, "Timer", "A", "timer", true, 15);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;
        if (data.isInVehicle() || isLowTPS()) return;

        long now = System.currentTimeMillis();
        long lastPacket = data.getLastPacketTime();

        if (lastPacket == 0) return;

        long elapsed = now - lastPacket;

        // Ignore massive gaps (lag spikes, alt-tab, etc.) — don't let them create huge negative balance
        if (elapsed > 2000) return;

        double balance = data.getBuffer("TimerA_balance");

        // Accumulate: positive = player sending too fast
        balance += (EXPECTED_MS_PER_TICK - elapsed);

        // Subtract tolerance each tick so minor jitter doesn't accumulate
        balance -= TOLERANCE_MS;

        // Clamp negative side so lag spikes don't give a huge "credit" to cheat with later
        if (balance < -200) balance = -200;

        // Only flag if balance exceeds 1 full second of debt (20 ticks ahead)
        // This means the player has consistently sent packets ~50% too fast for 2+ seconds
        if (balance > 1000) {
            double ticksAhead = balance / EXPECTED_MS_PER_TICK;
            double buffer = data.addBuffer("TimerA_flagBuffer", 1);
            if (buffer > 4) {
                flag(player, data, String.format("balance=%.0f ticksAhead=%.1f", balance, ticksAhead));
                data.setBuffer("TimerA_flagBuffer", 0);
                balance -= 500; // Don't fully reset — keep tracking if they continue
            }
        } else {
            data.decreaseBuffer("TimerA_flagBuffer", 0.5);
        }

        data.setBuffer("TimerA_balance", balance);
    }
}
