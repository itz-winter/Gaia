package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** AutoClicker (P) - Detects CPS exactly matching common autoclicker settings (e.g., 15, 20). */
public class AutoClickerP extends Check {
    public AutoClickerP(GaiaPlugin plugin) { super(plugin, "AutoClicker", "P", "autoclicker", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        int cps = data.getCPS();
        // Common autoclicker preset values
        if (cps == 15 || cps == 20 || cps == 25 || cps == 30 || cps == 50) {
            double buffer = data.addBuffer("ac_p_buffer", 1);
            if (buffer > 10) {
                flag(player, data, "presetCPS=" + cps);
                data.setBuffer("ac_p_buffer", 0);
            }
        } else {
            data.decreaseBuffer("ac_p_buffer", 1.0);
        }
    }
}
