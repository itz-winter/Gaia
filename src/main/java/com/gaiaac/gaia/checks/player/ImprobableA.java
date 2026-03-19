package com.gaiaac.gaia.checks.player;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Improbable (A) - Combines multiple low-confidence flags into a high-confidence detection.
 * Acts as a meta-check that aggregates suspicious behavior across multiple checks.
 */
public class ImprobableA extends Check {

    public ImprobableA(GaiaPlugin plugin) {
        super(plugin, "Improbable", "A", "player", true, 15);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        // Calculate total violations across all checks
        double totalVL = 0;
        int flaggedChecks = 0;

        for (var entry : data.getViolations().entrySet()) {
            double vl = entry.getValue();
            if (vl > 0) {
                totalVL += vl;
                flaggedChecks++;
            }
        }

        // If player has flags across many different checks, they're likely cheating
        if (flaggedChecks >= 5 && totalVL > 20) {
            double buffer = data.getBuffer("ImprobableA");
            buffer += flaggedChecks * 0.5;
            if (buffer > 8) {
                flag(player, data, String.format("checks=%d totalVL=%.1f", flaggedChecks, totalVL));
                data.setBuffer("ImprobableA", buffer / 2);
            } else {
                data.setBuffer("ImprobableA", buffer);
            }
        } else {
            data.setBuffer("ImprobableA", Math.max(0, data.getBuffer("ImprobableA") - 0.5));
        }
    }
}
