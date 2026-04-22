package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (J) - Detects duplicate movement packets (same position/rotation). */
public class BadPacketsJ extends Check {
    public BadPacketsJ(GaiaPlugin plugin) { super(plugin, "BadPackets", "J", "badpackets", true, 5); }
    @Override public void handle(Player player, PlayerData data) {
        // Legitimate idle sources of zero-delta packets: mining a block, standing in a menu,
        // AFK, server-side lag. Only flag after very sustained identical packets.
        if (data.isCurrentlyDigging() || data.isInventoryOpen()) {
            data.decreaseBuffer("bp_j_buffer", 1.0);
            return;
        }
        if (data.getDeltaX() == 0 && data.getDeltaY() == 0 && data.getDeltaZ() == 0
                && data.getDeltaYaw() == 0 && data.getDeltaPitch() == 0
                && data.isOnGround() == data.wasOnGround()) {
            double buffer = data.addBuffer("bp_j_buffer", 1);
            // 100 consecutive identical packets (~5s) is well past any legitimate idle scenario
            if (buffer > 100) { flag(player, data, "duplicatePacket"); data.setBuffer("bp_j_buffer", 0); }
        } else { data.decreaseBuffer("bp_j_buffer", 1.0); }
    }
}
