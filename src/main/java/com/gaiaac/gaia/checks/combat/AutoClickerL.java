package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** AutoClicker (L) - Detects clicking while inventory is open. */
public class AutoClickerL extends Check {
    public AutoClickerL(GaiaPlugin plugin) { super(plugin, "AutoClicker", "L", "autoclicker", true, 5); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (data.isInventoryOpen() && data.getCPS() > 5) {
            flag(player, data, 2.0, "clickWhileInventory cps=" + data.getCPS());
        }
    }
}
