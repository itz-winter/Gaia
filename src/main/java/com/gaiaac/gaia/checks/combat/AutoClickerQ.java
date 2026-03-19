package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** AutoClicker (Q) - Detects clicking that abruptly stops and starts (toggle detection). */
public class AutoClickerQ extends Check {
    public AutoClickerQ(GaiaPlugin plugin) { super(plugin, "AutoClicker", "Q", "autoclicker", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        int cps = data.getCPS();
        double lastCPS = data.getBuffer("ac_q_lastCPS");

        if (lastCPS > 12 && cps == 0) {
            data.addBuffer("ac_q_stops", 1);
        } else if (lastCPS == 0 && cps > 12) {
            double stops = data.getBuffer("ac_q_stops");
            if (stops > 3) {
                flag(player, data, "toggleClick stops=" + (int) stops + " cps=" + cps);
                data.setBuffer("ac_q_stops", 0);
            }
        }
        data.setBuffer("ac_q_lastCPS", cps);
    }
}
