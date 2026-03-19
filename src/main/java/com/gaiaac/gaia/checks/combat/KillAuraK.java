package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * KillAura (K) - Detects hitting while sprinting backwards (impossible in vanilla).
 */
public class KillAuraK extends Check {
    public KillAuraK(GaiaPlugin plugin) { super(plugin, "KillAura", "K", "killaura", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        long timeSinceAttack = System.currentTimeMillis() - data.getLastAttackTime();

        if (timeSinceAttack < 100 && data.isSprinting()) {
            // Check if moving backwards relative to yaw direction
            double moveAngle = Math.atan2(-data.getDeltaX(), data.getDeltaZ());
            double yawRad = Math.toRadians(data.getYaw());
            double angleDiff = Math.abs(moveAngle - yawRad);
            if (angleDiff > Math.PI) angleDiff = 2 * Math.PI - angleDiff;

            if (angleDiff > Math.PI * 0.7) {
                double buffer = data.addBuffer("ka_k_buffer", 1);
                if (buffer > 5) {
                    flag(player, data, "backwardsSprint angle=" + String.format("%.2f", Math.toDegrees(angleDiff)));
                    data.setBuffer("ka_k_buffer", 0);
                }
            } else {
                data.decreaseBuffer("ka_k_buffer", 0.5);
            }
        }
    }
}
