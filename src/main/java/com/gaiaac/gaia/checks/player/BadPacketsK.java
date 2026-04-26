package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (K) - Detects sprinting while using an item. */
public class BadPacketsK extends Check {
    public BadPacketsK(GaiaPlugin plugin) { super(plugin, "BadPackets", "K", "badpackets", true, 5); }
    @Override public void handle(Player player, PlayerData data) {
        // Use cached handRaised — player.isHandRaised() is Bukkit API and not safe on the netty thread
        if (data.isSprinting() && data.isHandRaised()) {
            double buffer = data.addBuffer("bp_k_buffer", 1);
            if (buffer > 5) { flag(player, data, "sprintWhileUsing"); data.setBuffer("bp_k_buffer", 0); }
        } else { data.decreaseBuffer("bp_k_buffer", 0.5); }
    }
}
