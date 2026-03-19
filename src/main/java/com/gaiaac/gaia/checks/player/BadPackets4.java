package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (4) - Detects invalid block face in placement packets. */
public class BadPackets4 extends Check {
    public BadPackets4(GaiaPlugin plugin) { super(plugin, "BadPackets", "4", "badpackets", true, 3); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
