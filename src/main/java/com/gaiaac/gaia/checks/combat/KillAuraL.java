package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * KillAura (L) - Detects switching targets too rapidly (multi-target aura).
 */
public class KillAuraL extends Check {
    public KillAuraL(GaiaPlugin plugin) { super(plugin, "KillAura", "L", "killaura", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        // Track target switching frequency via buffer
        java.util.UUID currentTarget = data.getLastTargetUUID();
        double lastTargetHash = data.getBuffer("ka_l_lastTarget");

        if (currentTarget != null) {
            double currentHash = currentTarget.hashCode();
            if (lastTargetHash != 0 && currentHash != lastTargetHash) {
                long timeSinceAttack = System.currentTimeMillis() - data.getLastAttackTime();
                if (timeSinceAttack < 200) {
                    double buffer = data.addBuffer("ka_l_buffer", 1);
                    if (buffer > 6) {
                        flag(player, data, "rapidTargetSwitch interval=" + timeSinceAttack);
                        data.setBuffer("ka_l_buffer", 0);
                    }
                }
            } else {
                data.decreaseBuffer("ka_l_buffer", 0.25);
            }
            data.setBuffer("ka_l_lastTarget", currentHash);
        }
    }
}
