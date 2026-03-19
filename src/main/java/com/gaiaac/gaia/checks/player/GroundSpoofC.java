package com.gaiaac.gaia.checks.player;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * GroundSpoof (C) - Detects rapid ground state toggling.
 * Flags when client rapidly alternates between ground and air states (NoFall patterns).
 */
public class GroundSpoofC extends Check {

    public GroundSpoofC(GaiaPlugin plugin) {
        super(plugin, "GroundSpoof", "C", "groundspoof", true, 6);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        if (data.isFlying()) return;
        if (data.isInVehicle() || data.isGliding() || data.isSwimming()) return;
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        boolean onGround = data.isOnGround();
        double deltaY = data.getY() - data.getLastY();

        // Detect rapid ground state toggling while moving vertically
        // NoFall often sends ground=true every other tick while falling
        if (onGround && deltaY < -0.08) {
            double buffer = data.getBuffer("GroundSpoofC");
            if (buffer > 4) {
                flag(player, data, String.format("deltaY=%.4f toggling", deltaY));
                data.setBuffer("GroundSpoofC", buffer / 2);
            } else {
                data.setBuffer("GroundSpoofC", buffer + 1.5);
            }
        } else {
            data.setBuffer("GroundSpoofC", Math.max(0, data.getBuffer("GroundSpoofC") - 0.5));
        }
    }
}
