package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (N) - Detects negative Y coordinate movement (below void). */
public class BadPacketsN extends Check {
    public BadPacketsN(GaiaPlugin plugin) { super(plugin, "BadPackets", "N", "badpackets", true, 3); }
    @Override public void handle(Player player, PlayerData data) {
        if (data.getY() < -128) {
            flag(player, data, 5.0, "belowVoid y=" + String.format("%.1f", data.getY()));
        }
    }
}
