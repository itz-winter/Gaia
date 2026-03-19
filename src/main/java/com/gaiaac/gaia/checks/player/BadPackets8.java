package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (8) - Detects invalid chat/command packet sequences. */
public class BadPackets8 extends Check {
    public BadPackets8(GaiaPlugin plugin) { super(plugin, "BadPackets", "8", "badpackets", true, 3); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
