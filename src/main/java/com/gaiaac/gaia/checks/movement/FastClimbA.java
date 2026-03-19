package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** FastClimb (A) - Detects ladder/vine speed exploits. */
public class FastClimbA extends Check {
    private static final double MAX_CLIMB_SPEED = 0.1176;

    public FastClimbA(GaiaPlugin plugin) { super(plugin, "FastClimb", "A", "fastclimb", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (!data.isOnClimbable() || recentlyTeleported(data) || data.isFlying()) return;
        if (data.getDeltaY() > MAX_CLIMB_SPEED + 0.01) {
            double buffer = data.addBuffer("fastclimb_a_buffer", 1);
            if (buffer > 3) {
                flag(player, data, 1.0, "climbSpeed=" + String.format("%.4f", data.getDeltaY()) + " max=" + MAX_CLIMB_SPEED);
                data.setBuffer("fastclimb_a_buffer", 0);
            }
        } else {
            data.decreaseBuffer("fastclimb_a_buffer", 0.5);
        }
    }
}
