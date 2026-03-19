package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * KillAura (G) - Detects attacking while looking away (no rotation towards target).
 */
public class KillAuraG extends Check {
    public KillAuraG(GaiaPlugin plugin) { super(plugin, "KillAura", "G", "killaura", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        float deltaYaw = data.getDeltaYaw();
        float deltaPitch = data.getDeltaPitch();
        long timeSinceAttack = System.currentTimeMillis() - data.getLastAttackTime();

        // Attack with no rotation change at all
        if (timeSinceAttack < 100 && deltaYaw < 0.01f && deltaPitch < 0.01f) {
            double buffer = data.addBuffer("ka_g_buffer", 1);
            if (buffer > 6) {
                flag(player, data, "noRotAttack dYaw=" + deltaYaw + " dPitch=" + deltaPitch);
                data.setBuffer("ka_g_buffer", 0);
            }
        } else {
            data.decreaseBuffer("ka_g_buffer", 0.5);
        }
    }
}
