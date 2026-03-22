package com.gaiaac.gaia.checks.player;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Timer (B) - Detects slow timer (sending packets slower than expected).
 * Some cheats reduce their game speed to bypass movement/flight checks.
 * Must be very lenient: normal players lag, tab out, or have WiFi jitter.
 *
 * Key: only flags when the player is ACTIVELY MOVING (significant deltaXZ + sprinting)
 * to prevent false positives from AFK players, loading screens, or idle connections.
 */
public class TimerB extends Check {

    private long lastTickNano = 0;

    public TimerB(GaiaPlugin plugin) {
        super(plugin, "Timer", "B", "timer", true, 15);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;
        if (data.isInVehicle() || isLowTPS()) return;

        long now = System.nanoTime();

        if (lastTickNano == 0) {
            lastTickNano = now;
            return;
        }

        long elapsed = now - lastTickNano;
        lastTickNano = now;

        // Ignore large gaps — player probably alt-tabbed, lagged, or loaded chunks
        if (elapsed > 3_000_000_000L) { // 3 seconds
            data.setBuffer("TimerB_balance", 0);
            return;
        }

        // Only accumulate balance when player is actually moving meaningfully
        if (data.getDeltaXZ() < 0.08) return;

        double balance = data.getBuffer("TimerB_balance");
        balance += (elapsed - 50_000_000L); // Positive = sending slower than expected (nanos)

        // Don't let fast packets create a big negative credit
        if (balance < -500_000_000L) balance = -500_000_000L;

        // Drain balance naturally (tolerance for jitter)
        balance -= 3_000_000L; // 3ms tolerance drain per tick

        // 3 seconds of sustained slowdown while actively moving is suspicious
        if (balance > 3_000_000_000L && data.getDeltaXZ() > 0.10 && data.isSprinting()) {
            double buffer = data.addBuffer("TimerB_flagBuffer", 1);
            if (buffer > 10) {
                flag(player, data, String.format("slow_balance=%.0fms", balance / 1_000_000.0));
                data.setBuffer("TimerB_flagBuffer", 0);
                balance -= 1_500_000_000L;
            }
        } else {
            data.decreaseBuffer("TimerB_flagBuffer", 0.25);
        }

        data.setBuffer("TimerB_balance", balance);
    }
}
