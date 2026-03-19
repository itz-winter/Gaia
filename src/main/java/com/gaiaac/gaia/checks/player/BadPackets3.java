package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (3) - Detects invalid digging status values. */
public class BadPackets3 extends Check {
    public BadPackets3(GaiaPlugin plugin) { super(plugin, "BadPackets", "3", "badpackets", true, 3); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
