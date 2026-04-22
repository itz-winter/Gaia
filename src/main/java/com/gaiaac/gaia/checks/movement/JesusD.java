package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Jesus (D) - Detects walking on water at abnormal speed. */
public class JesusD extends Check {
    public JesusD(GaiaPlugin plugin) { super(plugin, "Jesus", "D", "jesus", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data) || data.isInVehicle()) return;
        if (data.isRiptiding() || recentlyReceivedVelocity(data)) return;
        if (data.isInBubbleColumn()) return; // Bubble column propels player upward at high speed
        if (System.currentTimeMillis() - data.getLastExitWaterTime() < 800) return; // Leaping out of water

        // Vanilla sprint-swimming can reach ~0.39-0.50 dXZ; Dolphin's Grace can push it higher.
        // Threshold of 0.55 excludes all legitimate swimming while still catching Jesus (walking ON water).
        if (data.isInWater() && data.getDeltaY() >= 0 && !data.isOnGround() && data.getDeltaXZ() > 0.55) {
            double buffer = data.addBuffer("jesus_d_buffer", 1);
            if (buffer > 8) {
                flag(player, data, "waterSpeed dXZ=" + String.format("%.3f", data.getDeltaXZ())
                        + " dY=" + String.format("%.3f", data.getDeltaY()));
                data.setBuffer("jesus_d_buffer", 0);
            }
        } else {
            data.decreaseBuffer("jesus_d_buffer", 0.5);
        }
    }
}
