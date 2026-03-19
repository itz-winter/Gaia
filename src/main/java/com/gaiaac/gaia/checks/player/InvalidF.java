package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Invalid (F) - Detects swimming state without being in water. */
public class InvalidF extends Check {
    public InvalidF(GaiaPlugin plugin) { super(plugin, "Invalid", "F", "invalid", true, 5); }
    @Override public void handle(Player player, PlayerData data) {
        if (data.isSwimming() && !data.isInWater()) {
            double buffer = data.addBuffer("inv_f_buffer", 1);
            if (buffer > 5) { flag(player, data, "swimNoWater"); data.setBuffer("inv_f_buffer", 0); }
        } else { data.decreaseBuffer("inv_f_buffer", 0.5); }
    }
}
