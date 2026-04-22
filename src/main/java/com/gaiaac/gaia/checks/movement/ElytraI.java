package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Elytra (I) - Detects starting elytra flight from ground (no jump). */
public class ElytraI extends Check {
    public ElytraI(GaiaPlugin plugin) { super(plugin, "Elytra", "I", "elytra", true, 5); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (!data.isGliding() || recentlyTeleported(data)) return;

        // When a player lands from elytra glide, the server receives onGround=true in the same
        // physics tick that gliding is still true — EntityToggleGlideEvent fires the next tick.
        // Require the player to have been on the ground for 2+ consecutive ticks while gliding
        // to distinguish a real ground-glide exploit from the normal landing frame.
        if (data.isOnGround() && data.wasOnGround() && data.isGliding()) {
            double buffer = data.addBuffer("elytra_i_buffer", 1);
            if (buffer > 3) {
                flag(player, data, "groundElytra");
                data.setBuffer("elytra_i_buffer", 0);
            }
        } else {
            data.decreaseBuffer("elytra_i_buffer", 1.0);
        }
    }
}
