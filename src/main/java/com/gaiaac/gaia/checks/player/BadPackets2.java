package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (2) - Detects invalid window ID in inventory packets. */
public class BadPackets2 extends Check {
    public BadPackets2(GaiaPlugin plugin) { super(plugin, "BadPackets", "2", "badpackets", true, 3); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
