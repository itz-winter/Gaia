package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Aim (R) - Detects aim that always targets head level (pitch consistency).
 */
public class AimR extends Check {
    public AimR(GaiaPlugin plugin) { super(plugin, "Aim", "R", "aim", true, 12); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        // Disabled — this check is fundamentally flawed.
        // Looking straight ahead with low pitch change is what every normal player does.
        // Needs complete redesign to compare pitch consistency relative to a tracked target entity.
    }
}
