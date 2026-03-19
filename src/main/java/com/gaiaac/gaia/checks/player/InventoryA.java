package com.gaiaac.gaia.checks.player;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Inventory (A) - Detects moving while in inventory.
 * Vanilla client cannot send movement packets with significant deltas while inventory is open.
 */
public class InventoryA extends Check {

    public InventoryA(GaiaPlugin plugin) {
        super(plugin, "Inventory", "A", "player", true, 8);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        // Requires tracking inventory open state from CLICK_WINDOW packets
        // If player sends significant movement while inventory is open, flag
        if (data.isInVehicle() || data.isFlying()) return;
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        long lastInventoryClick = data.getLastPacketTime(); // simplified - would track inv state
        // Placeholder - needs inventory state tracking in PlayerData
    }
}
