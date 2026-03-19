package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** AutoClicker (S) - Detects clicking while blocking (impossible in vanilla). */
public class AutoClickerS extends Check {
    public AutoClickerS(GaiaPlugin plugin) { super(plugin, "AutoClicker", "S", "autoclicker", true, 5); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (data.isSneaking() && data.getCPS() > 10) {
            // Shield blocking typically reduces CPS significantly
            double buffer = data.addBuffer("ac_s_buffer", 1);
            if (buffer > 8) {
                flag(player, data, "clickWhileBlocking cps=" + data.getCPS());
                data.setBuffer("ac_s_buffer", 0);
            }
        } else {
            data.decreaseBuffer("ac_s_buffer", 0.5);
        }
    }
}
