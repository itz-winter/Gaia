package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (E) - Detects packet flood. */
public class BadPacketsE extends Check {
    public BadPacketsE(GaiaPlugin plugin) { super(plugin, "BadPackets", "E", "badpackets", true, 5); }
    @Override
    public void handle(Player player, PlayerData data) {
        int packetCount = data.getPacketTimestamps().size();
        if (packetCount > 100) { // More than 100 packets in 2 seconds
            flag(player, data, 2.0, "packetFlood count=" + packetCount);
        }
    }
}
