package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Elytra (C) - Detects elytra flight without firework. */
public class ElytraC extends Check {
    public ElytraC(GaiaPlugin plugin) { super(plugin, "Elytra", "C", "elytra", true, 8); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
