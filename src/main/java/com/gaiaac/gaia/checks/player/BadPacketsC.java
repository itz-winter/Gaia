package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (C) - Detects duplicate position packets. */
public class BadPacketsC extends Check {
    public BadPacketsC(GaiaPlugin plugin) { super(plugin, "BadPackets", "C", "badpackets", true, 5); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
