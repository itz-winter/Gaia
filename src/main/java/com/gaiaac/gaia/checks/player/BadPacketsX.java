package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (X) - Detects extremely high Y position (above build limit). */
public class BadPacketsX extends Check {
    public BadPacketsX(GaiaPlugin plugin) { super(plugin, "BadPackets", "X", "badpackets", true, 3); }
    @Override public void handle(Player player, PlayerData data) {
        if (data.getY() > 400) {
            flag(player, data, 5.0, "aboveBuildLimit y=" + String.format("%.1f", data.getY()));
        }
    }
}
