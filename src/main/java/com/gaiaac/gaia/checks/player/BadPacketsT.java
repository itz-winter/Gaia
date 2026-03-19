package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (T) - Detects entity interaction with self. */
public class BadPacketsT extends Check {
    public BadPacketsT(GaiaPlugin plugin) { super(plugin, "BadPackets", "T", "badpackets", true, 3); }
    @Override public void handle(Player player, PlayerData data) {
        // Tracked via interact entity packet in PacketManager
    }

    @Override
    public boolean isImplemented() { return false; }
}
