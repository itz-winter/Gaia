package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (1) - Detects invalid entity ID in interaction packets. */
public class BadPackets1 extends Check {
    public BadPackets1(GaiaPlugin plugin) { super(plugin, "BadPackets", "1", "badpackets", true, 3); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
