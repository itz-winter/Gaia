package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Scaffold (D) - Detects scaffold speed anomalies. */
public class ScaffoldD extends Check {
    public ScaffoldD(GaiaPlugin plugin) { super(plugin, "Scaffold", "D", "scaffold", true, 8); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
