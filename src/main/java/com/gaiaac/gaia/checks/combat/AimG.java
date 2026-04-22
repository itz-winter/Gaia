package com.gaiaac.gaia.checks.combat;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Aim (G) - BackAura: attacking while facing opposite to sprint direction.
 *
 * Sprint in vanilla requires the forward key — the player can only sprint in the
 * direction they are facing (± strafing offset of ~45° max). This means:
 *
 *   sprint movement direction ≈ player yaw  (within ~45°)
 *
 * If a player is SPRINTING and ATTACKING and their yaw is >130° away from their
 * movement direction, they are facing almost exactly backwards while sprinting forward
 * and hitting something behind them — the classic "BackAura" / "SpinKillAura" pattern.
 *
 * This cannot produce a false positive on legitimate backpedaling (you cannot sprint
 * backwards in vanilla) or strafing (max ~45° divergence while strafing-sprint).
 *
 * Detection requires all three simultaneously:
 *   1. Attacked within the last 500ms
 *   2. Sprinting at meaningful speed (dXZ > 0.18)
 *   3. Yaw is >130° divergent from movement direction
 */
public class AimG extends Check {
    public AimG(GaiaPlugin plugin) { super(plugin, "Aim", "G", "aim", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;
        if (recentlyReceivedVelocity(data)) return;
        if (!data.isSprinting()) return;

        long timeSinceAttack = System.currentTimeMillis() - data.getLastAttackTime();
        if (timeSinceAttack > 500) return;

        double dXZ = data.getDeltaXZ();
        if (dXZ < 0.18) return; // must be clearly moving to determine a direction

        // Movement bearing in Minecraft coordinate space:
        //   yaw 0° = south (+Z), yaw 90° = west (-X)
        //   atan2(-dX, dZ) produces a bearing matching Minecraft yaw convention
        double moveAngle = Math.toDegrees(Math.atan2(-data.getDeltaX(), data.getDeltaZ()));
        float yaw = data.getYaw();

        // Wrap the yaw-minus-moveAngle difference into [-180, 180]
        double diff = ((yaw - moveAngle) % 360 + 540) % 360 - 180;

        // >130° divergence while sprinting+attacking = mechanically impossible in vanilla
        if (Math.abs(diff) > 130) {
            double buffer = data.addBuffer("aim_g_buffer", 1);
            if (buffer > 5) {
                flag(player, data, 2.0, "backAura yaw=" + String.format("%.1f", yaw)
                        + " moveAngle=" + String.format("%.1f", moveAngle)
                        + " diff=" + String.format("%.1f", diff)
                        + " dXZ=" + String.format("%.3f", dXZ));
                data.setBuffer("aim_g_buffer", 2);
            }
        } else {
            data.decreaseBuffer("aim_g_buffer", 0.5);
        }
    }
}

