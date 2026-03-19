package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (L) - Detects extremely fast packet rate. */
public class BadPacketsL extends Check {
    public BadPacketsL(GaiaPlugin plugin) { super(plugin, "BadPackets", "L", "badpackets", true, 5); }
    @Override public void handle(Player player, PlayerData data) {
        java.util.List<Long> timestamps = data.getPacketTimestamps();
        long now = System.currentTimeMillis();
        int count = 0;
        for (int i = timestamps.size() - 1; i >= 0; i--) {
            if (now - timestamps.get(i) < 1000) count++;
            else break; // timestamps are ordered, so stop early
        }
        if (count > 30) {
            flag(player, data, 2.0, "packetFlood count=" + count + "/s");
        }
    }
}
