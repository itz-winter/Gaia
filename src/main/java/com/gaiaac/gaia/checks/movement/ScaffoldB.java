package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Scaffold (B) - Detects scaffold rotation alignment patterns. */
public class ScaffoldB extends Check {
    public ScaffoldB(GaiaPlugin plugin) { super(plugin, "Scaffold", "B", "scaffold", true, 8); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
