package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Baritone (A) - Detects bot pathing patterns (perfectly straight lines). */
public class BaritoneA extends Check {
    public BaritoneA(GaiaPlugin plugin) { super(plugin, "Baritone", "A", "baritone", true, 15); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
