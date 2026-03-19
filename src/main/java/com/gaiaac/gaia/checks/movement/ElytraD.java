package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Elytra (D) - Detects elytra anti-collision. */
public class ElytraD extends Check {
    public ElytraD(GaiaPlugin plugin) { super(plugin, "Elytra", "D", "elytra", true, 8); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
