package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (Y) - Detects no packets received for extended period then burst. */
public class BadPacketsY extends Check {
    public BadPacketsY(GaiaPlugin plugin) { super(plugin, "BadPackets", "Y", "badpackets", true, 5); }
    @Override public void handle(Player player, PlayerData data) {
        long gap = data.getTimeSinceLastPacket();
        if (gap > 3000 && data.getDeltaXZ() > 1.0) {
            flag(player, data, "packetBurst gap=" + gap + "ms dXZ=" + String.format("%.3f", data.getDeltaXZ()));
        }
    }
}
