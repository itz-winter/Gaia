package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (U) - Detects vehicle entering while already in vehicle. */
public class BadPacketsU extends Check {
    public BadPacketsU(GaiaPlugin plugin) { super(plugin, "BadPackets", "U", "badpackets", true, 3); }
    @Override public void handle(Player player, PlayerData data) {
        // Tracked via steer vehicle packets
    }

    @Override
    public boolean isImplemented() { return false; }
}
