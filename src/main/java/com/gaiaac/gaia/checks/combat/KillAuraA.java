package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * KillAura (A) - Detects multi-target attacks within impossible time frames.
 * Vanilla Minecraft can only target one entity per tick (50ms).
 * Tracks rapid target switching via PacketManager entity ID tracking.
 */
public class KillAuraA extends Check {

    public KillAuraA(GaiaPlugin plugin) {
        super(plugin, "KillAura", "A", "killaura", true, 10);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data)) return;

        int targetCount = data.getAttackTargetCount();

        // More than 2 distinct targets attacked within the rapid window
        if (targetCount > 2) {
            double buffer = data.addBuffer("killaura_a_buffer", targetCount - 1);
            if (buffer > 4) {
                flag(player, data, 2.0, "multiTarget targets=" + targetCount
                        + " switchTime=" + (System.currentTimeMillis() - data.getLastAttackTargetSwitchTime()) + "ms");
                data.setBuffer("killaura_a_buffer", 1);
                data.setAttackTargetCount(1);
            }
        } else {
            data.decreaseBuffer("killaura_a_buffer", 0.5);
        }
    }
}
