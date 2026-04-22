package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/**
 * NoSlow (B) - Detects NoSlow via sneaking speed analysis.
 * Sneaking reduces movement speed by the SNEAKING_SPEED attribute multiplier (default 0.3).
 * Swift Sneak enchantment (I/II/III) increases this attribute: +15%/+30%/+45% per level.
 * At Swift Sneak III the attribute is ~0.75, meaning sneak speed is 2.5× normal.
 * Uses the cached SNEAKING_SPEED attribute value to dynamically scale the max allowed speed.
 */
public class NoSlowB extends Check {
    private static final double BASE_SNEAK_SPEED = 0.065; // max sneak speed at default attribute (sneakingSpeed=0.3, moveSpeed=0.1)
    private static final double DEFAULT_SNEAKING_SPEED = 0.3;

    public NoSlowB(GaiaPlugin plugin) { super(plugin, "NoSlow", "B", "noslow", true, 8); }
    
    @Override
    public void handle(Player player, PlayerData data) {
        if (!data.isSneaking() || !data.isOnGround()) return;
        if (recentlyTeleported(data) || recentlyReceivedVelocity(data) || data.isFlying() || data.isInVehicle()) return;
        if (data.isInWater() || data.isInLava() || data.isOnIce() || data.isOnSlime() || data.isOnClimbable()) return;
        // Spam-crouch FP: PlayerToggleSneakEvent fires on main thread while movement packets arrive on netty
        // thread immediately. Give 500ms for the client to actually decelerate to sneak speed.
        // (Increased from 300ms — swift sneak III players with rapid toggle still slipped through at 300ms)
        if (System.currentTimeMillis() - data.getLastSneakToggleTime() < 500) return;
        
        double speedScale = data.getMovementSpeedAttribute() / 0.1;
        // Scale sneak speed by the SNEAKING_SPEED attribute (captures Swift Sneak enchantment)
        double sneakScale = data.getSneakingSpeedAttribute() / DEFAULT_SNEAKING_SPEED;
        double maxSneak = BASE_SNEAK_SPEED * speedScale * sneakScale + (data.getPing() / 1000.0);        if (data.getDeltaXZ() > maxSneak) {
            double buffer = data.addBuffer("noslow_b_buf", 1);
            if (buffer > 3) {
                flag(player, data, 1.5, "sneakSpeed=" + String.format("%.4f", data.getDeltaXZ())
                        + " max=" + String.format("%.4f", maxSneak));
                data.setBuffer("noslow_b_buf", 1);
            }
        } else {
            data.decreaseBuffer("noslow_b_buf", 0.25);
        }
    }
}
