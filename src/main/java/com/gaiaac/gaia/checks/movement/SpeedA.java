package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Speed (A) - Detects horizontal velocity exceeding physics limits.
 * Uses friction-based prediction: ground friction = 0.91 * 0.6 (slipperiness), air friction = 0.91
 *
 * Physics are scaled by the player's GENERIC_MOVEMENT_SPEED attribute value, which already
 * includes ALL modifiers: speed potions, /attribute command, plugin modifications, etc.
 * Default attribute value is 0.1 → max sprint momentum ≈ 0.36 blocks/tick.
 */
public class SpeedA extends Check {
    // Base physics constants at default movement speed (attribute = 0.1)
    private static final double BASE_MOVEMENT_SPEED = 0.1;
    private static final double BASE_MAX_GROUND_SPRINT = 0.36; // sprint with momentum at attr=0.1
    private static final double BASE_AIR_ACCEL = 0.026;        // sprint air acceleration at attr=0.1

    public SpeedA(GaiaPlugin plugin) { super(plugin, "Speed", "A", "speed", true, 10); }
    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || data.isFlying() || data.isInVehicle() || data.isGliding() || data.isWearingElytra()
                || recentlyReceivedVelocity(data) || data.isInWater() || data.isInLava()) return;
        if (data.isRiptiding()) return;

        double dXZ = data.getDeltaXZ();
        double lastDXZ = data.getLastDeltaXZ();
        // Scale factor relative to the default attribute value — captures potions, /attribute, plugin boosts
        double speedScale = data.getMovementSpeedAttribute() / BASE_MOVEMENT_SPEED;

        // === Ground speed check ===
        if (data.isOnGround()) {
            double maxSpeed = BASE_MAX_GROUND_SPRINT * speedScale;

            // Ice is very slippery — allow extra momentum
            if (data.isOnIce()) maxSpeed += 0.4;

            // Lag compensation
            maxSpeed += data.getPing() / 1000.0;

            if (dXZ > maxSpeed) {
                double buffer = data.addBuffer("speed_a_buffer", 1);
                if (buffer > 3) {
                    flag(player, data, 1.5, "groundSpeed=" + String.format("%.4f", dXZ) + " max=" + String.format("%.4f", maxSpeed));
                    data.setBuffer("speed_a_buffer", 1);
                }
            } else {
                data.decreaseBuffer("speed_a_buffer", 0.25);
            }
        }
        // === Air speed check ===
        else if (data.getAirTicks() > 1) {
            // In air: friction = 0.91, air accel scales with movement speed attribute
            double airAccel = BASE_AIR_ACCEL * speedScale;
            double expectedMax = lastDXZ * 0.91 + airAccel;

            // Generous tolerance for lag
            expectedMax += 0.05 + (data.getPing() / 500.0);

            if (dXZ > expectedMax && dXZ > 0.3) {
                double buffer = data.addBuffer("speed_air_buffer", 1);
                if (buffer > 4) {
                    flag(player, data, 1.5, "airSpeed=" + String.format("%.4f", dXZ) + " max=" + String.format("%.4f", expectedMax));
                    data.setBuffer("speed_air_buffer", 1);
                }
            } else {
                data.decreaseBuffer("speed_air_buffer", 0.25);
            }
        }
    }
}
