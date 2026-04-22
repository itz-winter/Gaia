package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/**
 * Strafe (A) - Detects abnormal sideways acceleration.
 * In vanilla, sprint is only possible in the forward direction (relative to facing yaw).
 * If a player's movement direction is perpendicular to their facing direction at sprint speed,
 * they are using a strafe-sprint or path-following hack (like Baritone).
 */
public class StrafeA extends Check {
    public StrafeA(GaiaPlugin plugin) { super(plugin, "Strafe", "A", "strafe", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || data.isFlying() || data.isInVehicle() || data.isGliding()) return;
        if (data.isInWater() || data.isInLava() || recentlyReceivedVelocity(data)) return;
        if (data.isRiptiding()) return;

        double dXZ = data.getDeltaXZ();
        if (dXZ < 0.15) return; // Not moving enough to analyze

        // Compute how much of the movement is "forward" vs "lateral" relative to facing yaw
        double yawRad = Math.toRadians(data.getYaw());
        double forwardX = -Math.sin(yawRad);
        double forwardZ =  Math.cos(yawRad);

        double forwardComponent = data.getDeltaX() * forwardX + data.getDeltaZ() * forwardZ;
        // |lateral| = sqrt(dXZ² - forward²), clamped to avoid sqrt of negative from floating-point
        double lateralSq = dXZ * dXZ - forwardComponent * forwardComponent;
        double lateralComponent = lateralSq > 0 ? Math.sqrt(lateralSq) : 0;

        double speedScale = data.getMovementSpeedAttribute() / 0.1;
        // Max vanilla lateral (strafing) speed: sprint diagonal gives lateral = dXZ/sqrt(2) ≈ 0.255 at 0.36 b/t
        // So legitimate max lateral at sprint = ~0.26 * speedScale
        // We flag only when lateral >> forward AND total speed is high (true sideways sprint hack)
        double maxLateral = 0.27 * speedScale + (data.getPing() / 1000.0);

        // Lateral > maxLateral AND forward component is tiny (< 0.05) = true sideways sprint
        if (lateralComponent > maxLateral && Math.abs(forwardComponent) < 0.08 && dXZ > 0.28 * speedScale) {
            double buffer = data.addBuffer("strafe_a_buf", 1);
            if (buffer > 4) {
                flag(player, data, 1.5, "strafeSprint lateral=" + String.format("%.4f", lateralComponent)
                        + " maxLateral=" + String.format("%.4f", maxLateral)
                        + " dXZ=" + String.format("%.4f", dXZ));
                data.setBuffer("strafe_a_buf", 2);
            }
        } else {
            data.decreaseBuffer("strafe_a_buf", 0.25);
        }
    }
}
