package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Jump (A) - Detects modified jump height. Vanilla jump height is 0.42 blocks/tick.
 *
 * Uses the player's GENERIC_JUMP_STRENGTH attribute (added in MC 1.21), which includes ALL modifiers:
 * jump boost potions, /attribute, plugin modifications, etc. Falls back to potion amplifier
 * calculation for pre-1.21 compatibility.
 */
public class JumpA extends Check {
    private static final double DEFAULT_JUMP_HEIGHT = 0.42;
    public JumpA(GaiaPlugin plugin) { super(plugin, "Jump", "A", "jump", true, 8); }
    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || data.isFlying() || data.isGliding() || data.isWearingElytra() || data.isInVehicle()
                || data.isInWater() || data.isInLava() || recentlyReceivedVelocity(data) || data.isOnClimbable()) return;
        // Honey blocks reduce jump height significantly — skip to avoid FP
        if (data.isOnHoneyBlock()) return;
        if (data.wasOnGround() && !data.isOnGround() && data.getDeltaY() > 0) {
            double jumpHeight = data.getDeltaY();
            // Use cached GENERIC_JUMP_STRENGTH attribute — includes jump boost potion + /attribute + plugins
            // Add a small tolerance (0.05) + lag compensation
            double maxAllowed = data.getJumpStrengthAttribute() + 0.05 + (data.getPing() / 500.0);
            if (jumpHeight > maxAllowed) {
                flag(player, data, 1.5, "jumpHeight=" + String.format("%.4f", jumpHeight) + " max=" + String.format("%.4f", maxAllowed));
            }
        }
    }
}
