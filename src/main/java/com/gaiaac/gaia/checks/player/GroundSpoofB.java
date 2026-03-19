package com.gaiaac.gaia.checks.player;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * GroundSpoof (B) - Detects false ground state by checking block below.
 * Verifies that the block below the player actually exists when ground is reported.
 */
public class GroundSpoofB extends Check {

    public GroundSpoofB(GaiaPlugin plugin) {
        super(plugin, "GroundSpoof", "B", "groundspoof", true, 8);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        // Disabled: requires world.getBlock() calls which are not safe on netty thread.
        // Would need to cache chunk data or schedule to main thread.
    }

    @Override
    public boolean isImplemented() { return false; }
}
