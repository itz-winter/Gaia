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
                || recentlyReceivedVelocity(data) || data.isOnIce()) return;

        // Use packet-tracked item usage instead of Bukkit API (thread-safe)
        boolean isUsingItem = data.isUsingItem();
        long timeSinceUse = System.currentTimeMillis() - data.getLastItemUseTime();

        // Clear stale item use state (if > 5 seconds, they probably stopped)
        if (timeSinceUse > 5000) {
            data.setUsingItem(false);
            return;
        }

        if (!isUsingItem) return;

        // Max speed while using item (with generous tolerance)
        double maxItemSpeed = 0.13 + (data.getPing() / 1000.0);

        // Speed effect — try-catch for thread safety
        try {
            if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SPEED)) {
                int amp = player.getPotionEffect(org.bukkit.potion.PotionEffectType.SPEED).getAmplifier();
                maxItemSpeed += (amp + 1) * 0.03;
            }
        } catch (Exception ignored) {
            maxItemSpeed += 0.15;
        }

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
