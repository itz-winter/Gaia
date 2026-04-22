package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/**
 * Jump (B) - Detects extra jumps (multi-jump).
 * While airborne (airTicks > 3), dY should be decreasing each tick due to gravity.
 * A sudden upward spike in dY while airborne indicates a second/extra jump from mid-air.
 */
public class JumpB extends Check {
    public JumpB(GaiaPlugin plugin) { super(plugin, "Jump", "B", "jump", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || data.isFlying() || data.isGliding() || data.isInVehicle()) return;
        if (data.isInWater() || data.isInLava() || data.isOnClimbable() || recentlyReceivedVelocity(data)) return;
        if (data.hasLevitation() || data.isOnGround()) return;
        if (data.isRiptiding() || data.isInBubbleColumn()) return;

        int airTicks = data.getAirTicks();
        if (airTicks < 4) return; // Jump initial phase — skip

        double dY = data.getDeltaY();
        double lastDY = data.getLastDeltaY();
        // After 4 air ticks, dY should be negative (falling) and decreasing.
        // If dY suddenly spikes UP (relative to last tick by more than 0.2), it's a mid-air jump.
        if (dY > lastDY + 0.20 && dY > 0.1) {
            double buffer = data.addBuffer("jump_b_buf", 1);
            if (buffer > 2) {
                flag(player, data, 2.0, "doubleJump dY=" + String.format("%.4f", dY)
                        + " lastDY=" + String.format("%.4f", lastDY) + " airTicks=" + airTicks);
                data.setBuffer("jump_b_buf", 1);
            }
        } else {
            data.decreaseBuffer("jump_b_buf", 0.5);
        }
    }
}
