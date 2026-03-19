package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Scaffold (C) - Detects downward placement patterns. */
public class ScaffoldC extends Check {
    public ScaffoldC(GaiaPlugin plugin) { super(plugin, "Scaffold", "C", "scaffold", true, 8); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
