package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * NoSlow (A) - Detects movement speed while using items (eating, blocking, drawing bow).
 * Uses packet-tracked item usage state instead of Bukkit API (thread-safe on netty thread).
 * When using an item, speed is reduced by 80% (multiplied by 0.2).
 * Max using-item speed: ~0.07 (walking) or ~0.09 (sprinting variant).
 */
public class NoSlowA extends Check {
    public NoSlowA(GaiaPlugin plugin) { super(plugin, "NoSlow", "A", "noslow", true, 8); }
    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || data.isFlying() || data.isInVehicle()
                || recentlyReceivedVelocity(data) || data.isOnIce()
                || data.isInWater() || data.isInLava() || data.isSwimming()) return;

        // Use packet-tracked item usage instead of Bukkit API (thread-safe)
        boolean isUsingItem = data.isUsingItem();
        long timeSinceUse = System.currentTimeMillis() - data.getLastItemUseTime();

        // Vanilla always cancels item use when sprinting starts.
        // If we see sprinting=true, the isUsingItem flag is stale — clear it and bail.
        if (data.isSprinting() && isUsingItem) {
            data.setUsingItem(false);
            return;
        }

        // Clear stale item use state — longest item-use animation (eating/drinking) is ~1.6s.
        // 2 seconds is generous enough for lag and bow-hold while standing still.
        if (timeSinceUse > 2000) {
            data.setUsingItem(false);
            return;
        }

        // Give 3 ticks (150ms) grace for the client to decelerate after starting item use
        if (timeSinceUse < 150) return;

        if (!isUsingItem) return;

        // Max speed while using item — scales with movement speed attribute (includes potions, /attribute, etc.)
        // Base is 0.13 at default attribute value of 0.1 (80% slow = speed * 0.2 * 1.3 sprint factor)
        double speedScale = data.getMovementSpeedAttribute() / 0.1;
        double maxItemSpeed = 0.13 * speedScale + (data.getPing() / 1000.0);

        if (data.getDeltaXZ() > maxItemSpeed && data.isOnGround()) {
            double buffer = data.addBuffer("noslow_a_buffer", 1);
            if (buffer > 3) {
                flag(player, data, 1.5, "noSlow speed=" + String.format("%.4f", data.getDeltaXZ())
                        + " max=" + String.format("%.4f", maxItemSpeed));
                data.setBuffer("noslow_a_buffer", 1);
            }
        } else {
            data.decreaseBuffer("noslow_a_buffer", 0.25);
        }
    }
}
