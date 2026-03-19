package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Hitbox (B) - Detects abnormal hit location offsets. */
public class HitboxB extends Check {
    public HitboxB(GaiaPlugin plugin) { super(plugin, "Hitbox", "B", "hitbox", true, 8); }

    @Override public boolean isImplemented() { return false; }
    @Override
    public void handle(Player player, PlayerData data) {
        // Advanced hitbox offset analysis
    }
}
