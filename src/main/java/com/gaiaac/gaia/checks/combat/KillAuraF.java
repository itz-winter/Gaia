package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** KillAura (F) - Detects post-attack rotation snapping back. */
public class KillAuraF extends Check {
    public KillAuraF(GaiaPlugin plugin) { super(plugin, "KillAura", "F", "killaura", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        // Snap-back rotation detection
    }

    @Override
    public boolean isImplemented() { return false; }
}
