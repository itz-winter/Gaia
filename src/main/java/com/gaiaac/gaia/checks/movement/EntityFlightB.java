package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** EntityFlight (B) - Detects entity hovering in air. */
public class EntityFlightB extends Check {
    public EntityFlightB(GaiaPlugin plugin) { super(plugin, "EntityFlight", "B", "entityflight", true, 5); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
