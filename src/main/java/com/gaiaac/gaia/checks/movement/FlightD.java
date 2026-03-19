package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Flight (D) - Detects glide (slow fall without feather falling). */
public class FlightD extends Check {
    public FlightD(GaiaPlugin plugin) { super(plugin, "Flight", "D", "flight", true, 10); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
