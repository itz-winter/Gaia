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

        if (deltaYaw > 15.0f && deltaPitch < 0.1f && deltaPitch >= 0) {
            double buffer = data.addBuffer("aim_m_buffer", 1);
            if (buffer > 12) {
                flag(player, data, "pitchLock dYaw=" + deltaYaw + " dPitch=" + deltaPitch);
                data.setBuffer("aim_m_buffer", 0);
            }
        } else {
            data.decreaseBuffer("aim_m_buffer", 0.5);
        }
    }
}
