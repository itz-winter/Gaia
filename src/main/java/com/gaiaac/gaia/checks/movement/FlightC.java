package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Flight (C) - Detects gravity inconsistency (wrong fall rate). */
public class FlightC extends Check {
    public FlightC(GaiaPlugin plugin) { super(plugin, "Flight", "C", "flight", true, 10); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
