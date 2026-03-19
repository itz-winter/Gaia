package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (M) - Detects flying flag set while not having permission. */
public class BadPacketsM extends Check {
    public BadPacketsM(GaiaPlugin plugin) { super(plugin, "BadPackets", "M", "badpackets", true, 3); }
    @Override public void handle(Player player, PlayerData data) {
        // Use cached gameMode instead of player.getAllowFlight() for thread safety
        if (data.isFlying() && data.getGameMode() != org.bukkit.GameMode.CREATIVE
                && data.getGameMode() != org.bukkit.GameMode.SPECTATOR) {
            flag(player, data, 3.0, "illegalFlyFlag");
        }
    }
}
