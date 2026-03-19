package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * KillAura (J) - Detects attacking from impossible pitch angles (head-snapping down/up).
 */
public class KillAuraJ extends Check {
    public KillAuraJ(GaiaPlugin plugin) { super(plugin, "KillAura", "J", "killaura", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        long timeSinceAttack = System.currentTimeMillis() - data.getLastAttackTime();
        float pitch = data.getPitch();

        if (timeSinceAttack < 100 && (pitch > 85.0f || pitch < -85.0f)) {
            double buffer = data.addBuffer("ka_j_buffer", 1);
            if (buffer > 4) {
                flag(player, data, "extremePitchAttack pitch=" + String.format("%.2f", pitch));
                data.setBuffer("ka_j_buffer", 0);
            }
        } else {
            data.decreaseBuffer("ka_j_buffer", 0.25);
        }
    }
}
