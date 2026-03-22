package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Aim (M) - Detects pitch staying within an impossibly narrow band while yaw changes wildly.
 */
public class AimM extends Check {
    public AimM(GaiaPlugin plugin) { super(plugin, "Aim", "M", "aim", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        float deltaYaw = data.getDeltaYaw();
        float deltaPitch = data.getDeltaPitch();

        // Must require very high yaw with literally zero pitch over multiple ticks
        // Normal players often turn horizontally with tiny pitch — only flag sustained zero pitch with huge yaw
        if (deltaYaw > 30.0f && deltaPitch == 0.0f && data.getLastDeltaPitch() == 0.0f) {
            double buffer = data.addBuffer("aim_m_buffer", 1);
            if (buffer > 15) {
                flag(player, data, "pitchLock dYaw=" + deltaYaw + " dPitch=" + deltaPitch);
                data.setBuffer("aim_m_buffer", 5);
            }
        } else {
            data.decreaseBuffer("aim_m_buffer", 1.0);
        }
    }
}
