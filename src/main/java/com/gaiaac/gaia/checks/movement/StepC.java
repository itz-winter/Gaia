package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/**
 * Step (C) - Detects step while moving at high speed.
 * Normal step-up requires the player to walk INTO a block face.
 * Stepping up > 0.6 blocks while moving at sprint speed is a step-speed exploit.
 */
public class StepC extends Check {
    public StepC(GaiaPlugin plugin) { super(plugin, "Step", "C", "step", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data) || data.isFlying() || data.isGliding()) return;
        if (data.isInVehicle() || recentlyReceivedVelocity(data)) return;
        if (data.getDeltaXZ() > 2.0) return; // Sanity check

        // Standard step detection + high speed check
        if (data.isOnGround() && data.wasOnGround() && data.getDeltaY() > data.getStepHeightAttribute() + 0.01) {
            double speedScale = data.getMovementSpeedAttribute() / 0.1;
            double sprintSpeed = 0.30 * speedScale;
            if (data.getDeltaXZ() > sprintSpeed) {
                flag(player, data, 2.0, "fastStep stepHeight=" + String.format("%.4f", data.getDeltaY())
                        + " dXZ=" + String.format("%.4f", data.getDeltaXZ())
                        + " sprintMax=" + String.format("%.4f", sprintSpeed));
            }
        }
    }
}
