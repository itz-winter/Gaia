package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (W) - Detects abnormal onGround flag flipping. */
public class BadPacketsW extends Check {
    public BadPacketsW(GaiaPlugin plugin) { super(plugin, "BadPackets", "W", "badpackets", true, 5); }
    @Override public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;
        if (data.isOnGround() != data.wasOnGround() && Math.abs(data.getDeltaY()) < 0.001) {
            double buffer = data.addBuffer("bp_w_buffer", 1);
            if (buffer > 15) { flag(player, data, "groundFlip dY=" + data.getDeltaY()); data.setBuffer("bp_w_buffer", 0); }
        } else { data.decreaseBuffer("bp_w_buffer", 0.5); }
    }
}
