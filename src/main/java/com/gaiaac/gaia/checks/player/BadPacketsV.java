package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (V) - Detects attacking while in vehicle. */
public class BadPacketsV extends Check {
    public BadPacketsV(GaiaPlugin plugin) { super(plugin, "BadPackets", "V", "badpackets", true, 5); }
    @Override public void handle(Player player, PlayerData data) {
        // Detect attacks while mounted (some are valid - horses)
    }

    @Override
    public boolean isImplemented() { return false; }
}
