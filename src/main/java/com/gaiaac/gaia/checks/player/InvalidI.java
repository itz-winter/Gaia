package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Invalid (I) - Detects impossible slot interaction sequences. */
public class InvalidI extends Check {
    public InvalidI(GaiaPlugin plugin) { super(plugin, "Invalid", "I", "invalid", true, 5); }
    @Override public void handle(Player player, PlayerData data) {
        // Tracked via inventory click packets
    }

    @Override
    public boolean isImplemented() { return false; }
}
