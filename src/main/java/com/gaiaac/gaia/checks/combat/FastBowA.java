package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** FastBow (A) - Detects arrow release faster than vanilla charge time. */
public class FastBowA extends Check {
    public FastBowA(GaiaPlugin plugin) { super(plugin, "FastBow", "A", "fastbow", true, 5); }

    @Override
    public void handle(Player player, PlayerData data) {
        // Bow charge detection is handled by use-item timing
        // Minimum charge time for full power is ~1000ms (20 ticks)
    }

    @Override
    public boolean isImplemented() { return false; }
}
