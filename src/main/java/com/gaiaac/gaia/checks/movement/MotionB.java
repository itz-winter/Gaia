package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/**
 * Motion (B) - Detects invalid vertical acceleration.
 * In vanilla, gravity always decelerates upward motion. After the initial jump phase (airTicks > 5),
 * dY must decrease every tick: dY_new = (dY_old - gravity) * drag ≈ (dY_old - 0.08) * 0.98.
 * If dY increases while airborne after the jump arc, something external is providing lift.
 */
public class MotionB extends Check {
    public MotionB(GaiaPlugin plugin) { super(plugin, "Motion", "B", "motion", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || data.isFlying() || data.isGliding() || data.isInVehicle()) return;
        if (data.isInWater() || data.isInLava() || data.isOnClimbable() || recentlyReceivedVelocity(data)) return;
        if (data.hasLevitation() || data.hasSlowFalling() || data.isOnGround()) return;
        if (data.isRiptiding() || data.isInBubbleColumn()) return;
        // Elytra deployment: the 4L task sets isGliding with up to 200ms lag.
        // For 600ms after glide starts (or if wearing elytra), allow normal vertical deviation.
        if (data.isWearingElytra() && System.currentTimeMillis() - data.getLastGlideStartTime() < 600) return;

        int airTicks = data.getAirTicks();
        if (airTicks < 6) return; // Skip initial jump arc (ticks 1-5 are the normal upswing)

        double dY = data.getDeltaY();
        double lastDY = data.getLastDeltaY();
        // After the jump arc, dY must strictly decrease each tick.
        // A tolerance of 0.02 absorbs floating-point rounding.
        if (dY > lastDY + 0.02) {
            double buffer = data.addBuffer("motion_b_buf", 1);
            if (buffer > 3) {
                flag(player, data, 2.0, "vertAccel dY=" + String.format("%.4f", dY)
                        + " lastDY=" + String.format("%.4f", lastDY) + " airTicks=" + airTicks);
                data.setBuffer("motion_b_buf", 1);
            }
        } else {
            data.decreaseBuffer("motion_b_buf", 0.25);
        }
    }
}
