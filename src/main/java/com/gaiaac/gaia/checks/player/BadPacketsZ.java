package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (Z) - Detects impossible health/food values in packets. */
public class BadPacketsZ extends Check {
    public BadPacketsZ(GaiaPlugin plugin) { super(plugin, "BadPackets", "Z", "badpackets", true, 3); }
    @Override public void handle(Player player, PlayerData data) {
        // Reserved for advanced packet-level health/food validation
    }

    @Override
    public boolean isImplemented() { return false; }
}
