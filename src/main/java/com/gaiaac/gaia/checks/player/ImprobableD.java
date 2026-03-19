package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Improbable (D) - Detects sustained high violation rates across multiple checks. */
public class ImprobableD extends Check {
    public ImprobableD(GaiaPlugin plugin) { super(plugin, "Improbable", "D", "improbable", true, 15); }
    @Override public void handle(Player player, PlayerData data) {
        double totalVL = 0;
        int checkCount = 0;
        for (double v : data.getViolations().values()) {
            totalVL += v;
            if (v > 0) checkCount++;
        }
        if (checkCount >= 3 && totalVL > 20) {
            flag(player, data, "multiCheckVL total=" + String.format("%.1f", totalVL) + " checks=" + checkCount);
        }
    }
}
