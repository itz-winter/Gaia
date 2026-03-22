package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Jump (A) - Detects modified jump height. Vanilla jump height is 0.42. */
public class JumpA extends Check {
    private static final double MAX_JUMP_HEIGHT = 0.42;
    public JumpA(GaiaPlugin plugin) { super(plugin, "Jump", "A", "jump", true, 8); }
    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || data.isFlying() || data.isGliding() || data.isInVehicle()
                || data.isInWater() || data.isInLava() || recentlyReceivedVelocity(data) || data.isOnClimbable()) return;
        if (data.wasOnGround() && !data.isOnGround() && data.getDeltaY() > 0) {
            double jumpHeight = data.getDeltaY();
            double maxAllowed = MAX_JUMP_HEIGHT + 0.05 + (data.getPing() / 500.0);
            // Account for jump boost effect — use cached amplifier (thread-safe)
            int jumpAmp = data.getJumpBoostAmplifier();
            if (jumpAmp >= 0) {
                maxAllowed += (jumpAmp + 1) * 0.1;
            }
            if (jumpHeight > maxAllowed) {
                flag(player, data, 1.5, "jumpHeight=" + String.format("%.4f", jumpHeight) + " max=" + String.format("%.4f", maxAllowed));
            }
        }
    }
}
