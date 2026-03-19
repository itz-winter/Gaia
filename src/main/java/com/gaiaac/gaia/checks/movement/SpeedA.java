package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Speed (A) - Detects horizontal velocity exceeding physics limits.
 * Uses friction-based prediction: ground friction = 0.91 * 0.6 (slipperiness), air friction = 0.91
 * Base sprint speed on ground: ~0.2806 (sprinting), ~0.2158 (walking)
 * Max first-tick sprint: 0.36 (with proper momentum)
 */
public class SpeedA extends Check {
    public SpeedA(GaiaPlugin plugin) { super(plugin, "Speed", "A", "speed", true, 10); }
    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || data.isFlying() || data.isInVehicle() || data.isGliding()
                || recentlyReceivedVelocity(data) || data.isInWater() || data.isInLava()) return;

        double dXZ = data.getDeltaXZ();
        double lastDXZ = data.getLastDeltaXZ();

        // === Ground speed check ===
        if (data.isOnGround()) {
            double maxSpeed = 0.36; // Base sprint speed with momentum

            // Speed effect — try-catch because Bukkit API may not be fully thread-safe
            try {
                if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SPEED)) {
                    int amp = player.getPotionEffect(org.bukkit.potion.PotionEffectType.SPEED).getAmplifier();
                    maxSpeed += (amp + 1) * 0.062;
                }
            } catch (Exception ignored) {
                maxSpeed += 0.2; // Assume possible speed effect if we can't read it
            }

            // Soul speed / ice handling
            if (data.isOnIce()) {
                maxSpeed += 0.4; // Ice is very slippery
            }

            // Lag compensation: moderate
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
            // In air, friction = 0.91. Max air speed is previous speed * 0.91 + 0.026 (sprint) or 0.02 (walk)
            double maxAirAccel = 0.026; // Sprint air acceleration
            double expectedMax = lastDXZ * 0.91 + maxAirAccel;

            // Speed effect boost — try-catch for thread safety
            try {
                if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SPEED)) {
                    int amp = player.getPotionEffect(org.bukkit.potion.PotionEffectType.SPEED).getAmplifier();
                    expectedMax += (amp + 1) * 0.02;
                }
            } catch (Exception ignored) {
                expectedMax += 0.15;
            }

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
