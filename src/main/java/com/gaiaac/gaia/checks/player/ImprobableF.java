package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Improbable (F) - Detects rapid VL accumulation rate. */
public class ImprobableF extends Check {
    public ImprobableF(GaiaPlugin plugin) { super(plugin, "Improbable", "F", "improbable", true, 20); }
    @Override public void handle(Player player, PlayerData data) {
        double totalVL = 0;
        for (double v : data.getViolations().values()) totalVL += v;
        long sessionTime = System.currentTimeMillis() - data.getJoinTime();
        if (sessionTime > 10000) {
            double vlPerMinute = totalVL / (sessionTime / 60000.0);
            if (vlPerMinute > 30) {
                flag(player, data, "rapidVL rate=" + String.format("%.1f", vlPerMinute) + "/min");
            }
        }
    }
}
