package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Baritone (B) - Detects Baritone rotation patterns. */
public class BaritoneB extends Check {
    public BaritoneB(GaiaPlugin plugin) { super(plugin, "Baritone", "B", "baritone", true, 15); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
