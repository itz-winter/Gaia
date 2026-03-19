package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (D) - Detects invalid entity interaction. */
public class BadPacketsD extends Check {
    public BadPacketsD(GaiaPlugin plugin) { super(plugin, "BadPackets", "D", "badpackets", true, 5); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
