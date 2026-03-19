package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * KillAura (B) - Detects silent rotations (attacking without facing target).
 * The player's client-reported rotation doesn't match the attack direction.
 */
public class KillAuraB extends Check {

    public KillAuraB(GaiaPlugin plugin) {
        super(plugin, "KillAura", "B", "killaura", true, 10);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data)) return;

        // Silent rotation detection - attack packets sent without corresponding rotation change
        long now = System.currentTimeMillis();
        if (now - data.getLastAttackTime() > 100) return;

        float deltaYaw = data.getDeltaYaw();
        float deltaPitch = data.getDeltaPitch();

        // Attacking while having zero rotation changes at high CPS suggests silent aim
        if (deltaYaw == 0 && deltaPitch == 0 && data.getCPS() > 5) {
            double buffer = data.addBuffer("killaura_b_buffer", 1);
            if (buffer > 3) {
                flag(player, data, 1.5, "silentRotation noRotChange cps=" + data.getCPS());
                data.setBuffer("killaura_b_buffer", 1);
            }
        } else {
            data.decreaseBuffer("killaura_b_buffer", 0.25);
        }
    }
}
