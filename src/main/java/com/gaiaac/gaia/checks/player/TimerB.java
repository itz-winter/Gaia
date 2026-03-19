package com.gaiaac.gaia.checks.player;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Timer (B) - Detects slow timer (sending packets slower than expected).
 * Some cheats reduce their game speed to bypass movement/flight checks.
 * Must be very lenient: normal players lag, tab out, or have WiFi jitter.
 */
public class TimerB extends Check {

    public TimerB(GaiaPlugin plugin) {
        super(plugin, "Timer", "B", "timer", true, 15);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;
        if (data.isInVehicle() || isLowTPS()) return;

        long now = System.currentTimeMillis();
        long lastPacket = data.getLastPacketTime();

        if (lastPacket == 0) return;

        long elapsed = now - lastPacket;

        // Ignore large gaps — player probably alt-tabbed, lagged, or loaded chunks
        if (elapsed > 3000) return;

        double balance = data.getBuffer("TimerB_balance");
        balance += (elapsed - 50.0); // Positive = sending slower than expected

        // Don't let fast packets create a big negative credit
        if (balance < -300) balance = -300;

        // 2500ms = player is consistently ~50 ticks behind over a sustained period
        if (balance > 2500 && data.getDeltaXZ() > 0.05) {
            double buffer = data.addBuffer("TimerB_flagBuffer", 1);
            if (buffer > 8) {
                flag(player, data, String.format("slow_balance=%.0f", balance));
                data.setBuffer("TimerB_flagBuffer", 0);
                balance -= 1000;
            }
        } else {
            data.decreaseBuffer("TimerB_flagBuffer", 0.25);
        }

        data.setBuffer("TimerB_balance", balance);
    }
}
