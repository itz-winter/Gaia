package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Invalid (H) - Detects sprinting with insufficient hunger level. */
public class InvalidH extends Check {
    public InvalidH(GaiaPlugin plugin) { super(plugin, "Invalid", "H", "invalid", true, 5); }
    @Override public void handle(Player player, PlayerData data) {
        try {
            if (data.isSprinting() && player.getFoodLevel() <= 6 && player.getGameMode() == org.bukkit.GameMode.SURVIVAL) {
                double buffer = data.addBuffer("inv_h_buffer", 1);
                if (buffer > 8) { flag(player, data, "sprintLowFood food=" + player.getFoodLevel()); data.setBuffer("inv_h_buffer", 0); }
            } else { data.decreaseBuffer("inv_h_buffer", 0.5); }
        } catch (Exception ignored) {}
    }
}
