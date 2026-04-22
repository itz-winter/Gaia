package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/**
 * FastUse (A) - Detects faster-than-normal item usage.
 * Each USE_ITEM packet should be separated by at least ~100ms (2 ticks).
 * Sending use-item packets faster bypasses the server-side use rate limit.
 * Note: PacketManager runs this check BEFORE updating lastItemUseTime,
 * so data.getLastItemUseTime() contains the PREVIOUS packet's timestamp.
 */
public class FastUseA extends Check {
    public FastUseA(GaiaPlugin plugin) { super(plugin, "FastUse", "A", "fastuse", true, 5); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (data.isFlying() || data.isInVehicle()) return;

        long lastUse = data.getLastItemUseTime();
        if (lastUse == 0) return; // No previous use

        long interval = System.currentTimeMillis() - lastUse;
        // < 80ms between USE_ITEM packets is below 2 ticks — faster than humanly possible
        if (interval < 80 && interval >= 0) {
            double buffer = data.addBuffer("fastuse_a_buf", 1);
            if (buffer > 5) {
                flag(player, data, 1.0, "fastUse interval=" + interval + "ms");
                data.setBuffer("fastuse_a_buf", 0);
            }
        } else {
            data.decreaseBuffer("fastuse_a_buf", 1.0);
        }
    }
}
