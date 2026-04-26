package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (Q) - Detects movement while dead. */
public class BadPacketsQ extends Check {
    public BadPacketsQ(GaiaPlugin plugin) { super(plugin, "BadPackets", "Q", "badpackets", true, 3); }
    @Override public void handle(Player player, PlayerData data) {
        // Use cached isDead — player.isDead() is Bukkit API and not safe on the netty thread
        if (data.isDead() && data.getDeltaXZ() > 0.1) {
            flag(player, data, 5.0, "moveWhileDead dXZ=" + String.format("%.3f", data.getDeltaXZ()));
        }
    }
}
