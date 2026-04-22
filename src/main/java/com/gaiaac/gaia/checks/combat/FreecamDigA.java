package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * FreecamDig (A) - Detects breaking blocks while looking away from them (FreecamDig / AirDig).
 *
 * Freecam hacks separate the camera position from the player body. The client sends a
 * PLAYER_DIGGING (START_DIGGING) packet with the target block coordinates, but the player's
 * yaw/pitch points in a completely different direction.
 *
 * Detection: compute the angle between the player's look direction (yaw/pitch at dig time)
 * and the direction vector from the player's eye to the centre of the target block.
 * Flag if the angle exceeds MAX_ANGLE (70°). This generously allows for offset digging
 * (digging a block slightly to the side) while still catching camera-decoupled freecam.
 *
 * Runs on PLAYER_DIGGING (START_DIGGING) packets only.
 */
public class FreecamDigA extends Check {

    private static final double MAX_ANGLE = 70.0;

    public FreecamDigA(GaiaPlugin plugin) {
        super(plugin, "FreecamDig", "A", "freecamdig", true, 10);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        // Read target block and player rotation captured at START_DIGGING time (on netty thread)
        final int bx = data.getLastDigTargetX();
        final int by = data.getLastDigTargetY();
        final int bz = data.getLastDigTargetZ();
        final float attackYaw   = data.getAttackYaw();
        final float attackPitch = data.getAttackPitch();
        // Player eye position at dig time
        final double eyeX = data.getX();
        final double eyeY = data.getY() + 1.62;
        final double eyeZ = data.getZ();

        // Schedule block lookup on main thread — Bukkit world API is not netty-safe
        Bukkit.getScheduler().runTask(plugin, () -> {
            Block block;
            try {
                block = player.getWorld().getBlockAt(bx, by, bz);
            } catch (Exception ignored) {
                return;
            }
            // Don't flag for air blocks (block may have just been broken or wasn't solid)
            if (block.isEmpty()) return;

            // Direction vector from player eye to centre of the target block
            double dx = (bx + 0.5) - eyeX;
            double dy = (by + 0.5) - eyeY;
            double dz = (bz + 0.5) - eyeZ;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist < 0.5) return; // Point-blank — angle is undefined

            // Convert direction vector to Minecraft yaw/pitch convention
            double dirYaw   = Math.toDegrees(Math.atan2(-dx, dz));
            double dirPitch = Math.toDegrees(Math.atan2(-dy, Math.sqrt(dx * dx + dz * dz)));

            // Angular divergence — wrap yaw into [0, 180]
            double yawDiff   = Math.abs(((attackYaw - dirYaw) % 360 + 540) % 360 - 180);
            double pitchDiff = Math.abs(attackPitch - dirPitch);
            double totalAngle = Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);

            if (totalAngle > MAX_ANGLE) {
                double buffer = data.addBuffer("freecamdig_a_buffer", 1);
                if (buffer > 3) {
                    flag(player, data, 2.0, "freecamDig angle=" + String.format("%.1f", totalAngle)
                            + " yawOff=" + String.format("%.1f", yawDiff)
                            + " pitchOff=" + String.format("%.1f", pitchDiff)
                            + " dist=" + String.format("%.2f", dist));
                    data.setBuffer("freecamdig_a_buffer", 1);
                }
            } else {
                data.decreaseBuffer("freecamdig_a_buffer", 0.5);
            }
        });
    }
}
