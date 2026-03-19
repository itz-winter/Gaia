package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (G) - Detects impossible pitch values (outside -90 to 90). */
public class BadPacketsG extends Check {
    public BadPacketsG(GaiaPlugin plugin) { super(plugin, "BadPackets", "G", "badpackets", true, 3); }
    @Override public void handle(Player player, PlayerData data) {
        float pitch = data.getPitch();
        if (pitch > 90.0f || pitch < -90.0f) {
            flag(player, data, 5.0, "invalidPitch=" + pitch);
        }
    }
}
