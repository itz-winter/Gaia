package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (6) - Detects invalid held slot index (outside 0-8). */
public class BadPackets6 extends Check {
    public BadPackets6(GaiaPlugin plugin) { super(plugin, "BadPackets", "6", "badpackets", true, 3); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
