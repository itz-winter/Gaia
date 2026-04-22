package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Motion (A) - Analyzes raw movement deltas for impossible acceleration.
 * Checks if horizontal acceleration exceeds physics limits.
 * Ground friction = 0.546 (0.91 * 0.6), max accel per tick ~0.1.
 */
public class MotionA extends Check {
    public MotionA(GaiaPlugin plugin) { super(plugin, "Motion", "A", "motion", true, 10); }
    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || data.isFlying() || data.isInVehicle()
                || recentlyReceivedVelocity(data) || data.isOnIce() || data.isOnSlime()
                || data.isInWater() || data.isInLava()) return;
        // Soul Speed enchantment increases acceleration on soul sand/soil beyond normal limits
        if (data.isOnSoulBlock()) return;
        if (data.isRiptiding()) return;

        double dXZ = data.getDeltaXZ();
        double lastDXZ = data.getLastDeltaXZ();
        double accel = dXZ - lastDXZ;

        // Impossible ground acceleration (normal max ~0.1 per tick with sprint)
        if (data.isOnGround() && accel > 0.15 && dXZ > 0.4) {
            double buffer = data.addBuffer("motion_a_buffer", 1);
            if (buffer > 3) {
                flag(player, data, 1.5, "accel=" + String.format("%.4f", accel)
                        + " speed=" + String.format("%.4f", dXZ));
                data.setBuffer("motion_a_buffer", 1);
            }
        } else {
            data.decreaseBuffer("motion_a_buffer", 0.25);
        }
    }
}
