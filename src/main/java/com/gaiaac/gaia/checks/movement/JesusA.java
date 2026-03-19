package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Jesus (A) - Detects walking on water/liquids. */
public class JesusA extends Check {
    public JesusA(GaiaPlugin plugin) { super(plugin, "Jesus", "A", "jesus", true, 8); }
    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || data.isFlying() || data.isInVehicle() || data.isGliding()) return;
        if (data.isInWater() && data.isOnGround() && data.getDeltaY() >= 0 && data.getDeltaXZ() > 0.1) {
            double buffer = data.addBuffer("jesus_a_buffer", 1);
            if (buffer > 4) {
                flag(player, data, 1.5, "waterWalk dXZ=" + String.format("%.4f", data.getDeltaXZ()));
                data.setBuffer("jesus_a_buffer", 0);
            }
        } else {
            data.decreaseBuffer("jesus_a_buffer", 0.5);
        }
    }
}
