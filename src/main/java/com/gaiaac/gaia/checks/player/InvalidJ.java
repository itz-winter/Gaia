package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Invalid (J) - Detects invalid game mode transitions. */
public class InvalidJ extends Check {
    public InvalidJ(GaiaPlugin plugin) { super(plugin, "Invalid", "J", "invalid", true, 3); }
    @Override public void handle(Player player, PlayerData data) {
        // Reserved for gamemode-specific packet validation
    }

    @Override
    public boolean isImplemented() { return false; }
}
