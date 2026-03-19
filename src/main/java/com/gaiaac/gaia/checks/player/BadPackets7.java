package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (7) - Detects invalid abilities packet flags. */
public class BadPackets7 extends Check {
    public BadPackets7(GaiaPlugin plugin) { super(plugin, "BadPackets", "7", "badpackets", true, 3); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
