package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Aim (V) - Detects non-GCD-conforming rotation deltas (invalid sensitivity).
 */
public class AimV extends Check {
    public AimV(GaiaPlugin plugin) { super(plugin, "Aim", "V", "aim", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        float deltaYaw = data.getDeltaYaw();
        float deltaPitch = data.getDeltaPitch();

        if (deltaYaw > 0.5f && deltaPitch > 0.5f) {
            double gcd = com.gaiaac.gaia.util.math.MathUtil.gcd(deltaYaw, deltaPitch);
            if (gcd < 0.0001 && deltaYaw > 2.0f && deltaPitch > 1.0f) {
                double buffer = data.addBuffer("aim_v_buffer", 1);
                if (buffer > 20) {
                    flag(player, data, "invalidGCD gcd=" + String.format("%.6f", gcd));
                    data.setBuffer("aim_v_buffer", 0);
                }
            } else {
                data.decreaseBuffer("aim_v_buffer", 0.5);
            }
        }
    }
}
