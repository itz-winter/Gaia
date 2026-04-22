package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Jesus (B) - Detects standing on water surface.
 * When a player has onGround=true while fully in water (not swimming), dY stable near 0,
 * and moving at walk speed, they are walking on the water surface (Jesus hack).
 */
public class JesusB extends Check {
    public JesusB(GaiaPlugin plugin) { super(plugin, "Jesus", "B", "jesus", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || data.isFlying() || data.isInVehicle() || data.isGliding()) return;
        if (!data.isInWater() || data.isSwimming() || data.isInBubbleColumn()) return; // Must be in water but NOT swimming

        // In water, onGround=true means feet touching a solid block or the water "surface"
        // Jesus hack: onGround=true in water while dY is near 0 (not sinking) and moving horizontally
        if (data.isOnGround() && Math.abs(data.getDeltaY()) < 0.05 && data.getDeltaXZ() > 0.08) {
            double buffer = data.addBuffer("jesus_b_buf", 1);
            if (buffer > 5) {
                flag(player, data, 1.5, "waterSurface dXZ=" + String.format("%.4f", data.getDeltaXZ())
                        + " dY=" + String.format("%.4f", data.getDeltaY()));
                data.setBuffer("jesus_b_buf", 2);
            }
        } else {
            data.decreaseBuffer("jesus_b_buf", 0.5);
        }
    }
}
