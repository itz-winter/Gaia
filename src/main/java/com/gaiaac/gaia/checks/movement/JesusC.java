package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/**
 * Jesus (C) - Detects lava walking.
 * Moving in lava is severely limited: ~0.02 b/t max horizontal speed in lava without Fire Resistance.
 * If a player moves at normal walking speed in lava, they are bypassing lava slowdown.
 */
public class JesusC extends Check {
    public JesusC(GaiaPlugin plugin) { super(plugin, "Jesus", "C", "jesus", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || data.isFlying() || data.isInVehicle() || data.isGliding()) return;
        if (!data.isInLava()) return;

        double dXZ = data.getDeltaXZ();
        // Max legitimate speed in lava: ~0.02 normally, ~0.12 with Depth Strider III (max)
        // We use 0.15 to be safe — anything above this is clearly a lava walk exploit
        double maxLavaSpeed = 0.15 + (data.getPing() / 1000.0);

        if (dXZ > maxLavaSpeed) {
            double buffer = data.addBuffer("jesus_c_buf", 1);
            if (buffer > 4) {
                flag(player, data, 1.5, "lavaWalk dXZ=" + String.format("%.4f", dXZ)
                        + " max=" + String.format("%.4f", maxLavaSpeed));
                data.setBuffer("jesus_c_buf", 2);
            }
        } else {
            data.decreaseBuffer("jesus_c_buf", 0.5);
        }
    }
}
