package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
import java.util.Map;
/** Improbable (E) - Detects combat combined with movement anomalies. */
public class ImprobableE extends Check {
    public ImprobableE(GaiaPlugin plugin) { super(plugin, "Improbable", "E", "improbable", true, 15); }
    @Override public void handle(Player player, PlayerData data) {
        double combatVL = 0, moveVL = 0;
        for (Map.Entry<String, Double> entry : data.getViolations().entrySet()) {
            String key = entry.getKey();
            double val = entry.getValue();
            if (key.startsWith("KillAura") || key.startsWith("Aim") || key.startsWith("Reach")) {
                combatVL += val;
            } else if (key.startsWith("Speed") || key.startsWith("Flight") || key.startsWith("Motion")) {
                moveVL += val;
            }
        }
        if (combatVL > 5 && moveVL > 5) {
            flag(player, data, "combatMovement cVL=" + String.format("%.1f", combatVL) + " mVL=" + String.format("%.1f", moveVL));
        }
    }
}
