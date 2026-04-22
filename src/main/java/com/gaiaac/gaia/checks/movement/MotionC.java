package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/**
 * Motion (C) - Detects invalid horizontal deceleration (instant air stop).
 * In vanilla, air friction decays horizontal speed by ~9% per tick (0.91 multiplier).
 * A player moving at 0.3+ b/t can't drop to near-zero in a single tick while airborne
 * unless they hit a wall (one occurrence) or teleport. Repeated occurrences are suspicious.
 */
public class MotionC extends Check {
    public MotionC(GaiaPlugin plugin) { super(plugin, "Motion", "C", "motion", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (data.isOnGround() || data.isFlying() || data.isInVehicle() || data.isGliding()) return;
        if (recentlyTeleported(data) || recentlyReceivedVelocity(data) || data.isInWater() || data.isInLava()) return;
        if (data.isRiptiding() || data.isInBubbleColumn()) return;

        int airTicks = data.getAirTicks();
        if (airTicks < 3) return;

        double dXZ = data.getDeltaXZ();
        double lastDXZ = data.getLastDeltaXZ();
        // Sudden stop in air: was moving 0.25+, now near-zero
        if (lastDXZ > 0.25 && dXZ < 0.02) {
            double buffer = data.addBuffer("motion_c_buf", 1);
            if (buffer > 4) {
                flag(player, data, 1.5, "airStop lastDXZ=" + String.format("%.4f", lastDXZ)
                        + " dXZ=" + String.format("%.4f", dXZ) + " airTicks=" + airTicks);
                data.setBuffer("motion_c_buf", 2);
            }
        } else {
            data.decreaseBuffer("motion_c_buf", 0.5);
        }
    }
}
