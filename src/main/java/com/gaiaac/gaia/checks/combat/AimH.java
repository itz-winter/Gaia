package com.gaiaac.gaia.checks.combat;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Aim (H) - Detects attacking entities while not facing them (FreecamAttack / BackAttack).
 * Freecam hacks allow the player's camera to move independently from their body — the client
 * sends an INTERACT_ENTITY (attack) packet while facing away from the target.
 * Checks the angle between the player's actual yaw/pitch at attack time and the
 * direction vector toward the attacked entity. Flags if the angle exceeds 65°.
 * Runs on INTERACT_ENTITY attack packets; entity lookup is scheduled on the main thread.
 */
public class AimH extends Check {

    /** Maximum angle (degrees) between look direction and target direction before flagging. */
    private static final double MAX_ANGLE = 65.0;

    public AimH(GaiaPlugin plugin) { super(plugin, "Aim", "H", "aim", true, 12); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        final int targetEntityId = data.getLastTargetEntityId();
        if (targetEntityId == -1) return;

        // Capture rotation at attack time (set in PacketManager INTERACT_ENTITY handler)
        final float attackYaw   = data.getAttackYaw();
        final float attackPitch = data.getAttackPitch();
        // Player eye position at attack time
        final double eyeX = data.getX();
        final double eyeY = data.getY() + 1.62;
        final double eyeZ = data.getZ();

        Bukkit.getScheduler().runTask(plugin, () -> {
            Entity target = null;
            try {
                for (Entity e : player.getNearbyEntities(8, 8, 8)) {
                    if (e.getEntityId() == targetEntityId) { target = e; break; }
                }
            } catch (Exception ignored) { return; }
            if (target == null) return;

            // Vector from player eye to the center of the entity's hitbox
            double dx = target.getLocation().getX() - eyeX;
            double dy = (target.getLocation().getY() + target.getHeight() / 2.0) - eyeY;
            double dz = target.getLocation().getZ() - eyeZ;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist < 1.0) return; // Too close — angle is meaningless at point-blank range

            // Convert direction vector to Minecraft yaw/pitch
            // Minecraft yaw: 0=south(+Z), 90=west(-X), 180=north(-Z), 270=east(+X)
            double dirYaw   = Math.toDegrees(Math.atan2(-dx, dz));
            double dirPitch = Math.toDegrees(Math.atan2(-dy, Math.sqrt(dx * dx + dz * dz)));

            // Yaw difference, wrapped to [0, 180]
            double yawDiff   = Math.abs(((attackYaw - dirYaw) % 360 + 540) % 360 - 180);
            double pitchDiff = Math.abs(attackPitch - dirPitch);

            // Total angular divergence (Euclidean in angle space)
            double totalAngle = Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);

            if (totalAngle > MAX_ANGLE) {
                double buffer = data.addBuffer("aim_h_buffer", 1);
                if (buffer > 3) {
                    flag(player, data, 2.0, "backAttack angle=" + String.format("%.1f", totalAngle)
                            + " yawOff=" + String.format("%.1f", yawDiff)
                            + " pitchOff=" + String.format("%.1f", pitchDiff));
                    data.setBuffer("aim_h_buffer", 1);
                }
            } else {
                data.decreaseBuffer("aim_h_buffer", 0.5);
            }
        });
    }
}
