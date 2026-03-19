package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Elytra (J) - Detects elytra pitch abuse (flying at impossible angles). */
public class ElytraJ extends Check {
    public ElytraJ(GaiaPlugin plugin) { super(plugin, "Elytra", "J", "elytra", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (!data.isGliding() || recentlyTeleported(data)) return;

        float pitch = data.getPitch();
        // Flying upward with a pitch looking down
        if (pitch > 30 && data.getDeltaY() > 0.3) {
            double buffer = data.addBuffer("elytra_j_buffer", 1);
            if (buffer > 5) {
                flag(player, data, "pitchAbuse pitch=" + pitch + " dY=" + String.format("%.3f", data.getDeltaY()));
                data.setBuffer("elytra_j_buffer", 0);
            }
        } else {
            data.decreaseBuffer("elytra_j_buffer", 0.5);
        }
    }
}
