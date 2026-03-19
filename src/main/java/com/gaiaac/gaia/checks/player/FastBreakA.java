package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** FastBreak (A) - Detects breaking blocks faster than vanilla limits. */
public class FastBreakA extends Check {
    public FastBreakA(GaiaPlugin plugin) { super(plugin, "FastBreak", "A", "fastbreak", true, 8); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
