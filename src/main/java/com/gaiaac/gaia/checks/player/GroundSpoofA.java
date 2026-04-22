package com.gaiaac.gaia.checks.player;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * GroundSpoof (A) - Detects client claiming to be on ground when airborne.
 * Compares client-reported ground state with server-side position checks.
 */
public class GroundSpoofA extends Check {

    public GroundSpoofA(GaiaPlugin plugin) {
        super(plugin, "GroundSpoof", "A", "groundspoof", true, 8);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        if (data.isFlying() || data.isGliding() || data.isSwimming()) return;
        if (data.isInVehicle() || data.isInWater() || data.isInLava()) return;
        if (recentlyTeleported(data) || recentlyJoined(data) || recentlyReceivedVelocity(data)) return;

        boolean clientOnGround = data.isOnGround();
        double deltaY = data.getDeltaY();

        // Client claims on ground but is falling significantly — classic NoFall pattern.
        // We only check negative deltaY (falling) here; positive deltaY (stepping up a block)
        // is legitimate and must NOT be flagged — step height is 0.6 and onGround=true after step.
        if (clientOnGround && deltaY < -0.1) {
            double buffer = data.addBuffer("groundspoof_a_buffer", 1);
            if (buffer > 3) {
                flag(player, data, 2.0, String.format("deltaY=%.4f clientGround=true", deltaY));
                data.setBuffer("groundspoof_a_buffer", 1);
            }
        }
        // Client claims on ground but just transitioned from not-on-ground with upward motion
        // (classic criticals/ground-spoof technique)
        else if (clientOnGround && !data.wasOnGround() && data.getLastDeltaY() > 0
                && deltaY < -0.01) {
            double buffer = data.addBuffer("groundspoof_a_buffer", 1);
            if (buffer > 3) {
                flag(player, data, 2.0, String.format("spoofLand lastDY=%.4f dY=%.4f", data.getLastDeltaY(), deltaY));
                data.setBuffer("groundspoof_a_buffer", 1);
            }
        } else {
            data.decreaseBuffer("groundspoof_a_buffer", 0.25);
        }
    }
}
