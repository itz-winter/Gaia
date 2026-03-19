package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (O) - Detects extremely large position deltas in a single tick. */
public class BadPacketsO extends Check {
    public BadPacketsO(GaiaPlugin plugin) { super(plugin, "BadPackets", "O", "badpackets", true, 3); }
    @Override public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;
        double dist = Math.sqrt(data.getDeltaX() * data.getDeltaX() + data.getDeltaY() * data.getDeltaY() + data.getDeltaZ() * data.getDeltaZ());
        if (dist > 10.0) {
            flag(player, data, 5.0, "largeMovement dist=" + String.format("%.2f", dist));
        }
    }
}
