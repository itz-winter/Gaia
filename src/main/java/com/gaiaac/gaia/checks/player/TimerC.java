package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Timer (C) - Detects sustained slow timer via low packet rate.
 * Counts movement packets over 1 second and flags if consistently too low
 * while the player is actively moving (ruling out AFK/standing still).
 *
 * Improved: higher buffer requirement, stricter movement threshold, and
 * faster buffer decay to prevent accumulation during transitional states.
 */
public class TimerC extends Check {
    public TimerC(GaiaPlugin plugin) { super(plugin, "Timer", "C", "timer", true, 15); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;
        if (data.isInVehicle() || isLowTPS()) return;

        java.util.List<Long> timestamps = data.getPacketTimestamps();
        if (timestamps.size() < 15) return;

        long now = System.currentTimeMillis();
        int count = 0;
        for (int i = timestamps.size() - 1; i >= 0; i--) {
            if (now - timestamps.get(i) < 1000) count++;
            else break;
        }

        // Only check if the player is actively sprinting with meaningful horizontal movement
        // Normal players send ~20 packets/sec; below 10 while actively sprinting fast is suspicious
        if (count < 10 && data.getDeltaXZ() > 0.20 && data.isSprinting()) {
            double buffer = data.addBuffer("timer_c_buffer", 1);
            if (buffer > 25) {
                flag(player, data, "slowTimer packets/s=" + count);
                data.setBuffer("timer_c_buffer", 12); // Don't fully reset
            }
        } else {
            data.decreaseBuffer("timer_c_buffer", 1.5);
        }
    }
}
