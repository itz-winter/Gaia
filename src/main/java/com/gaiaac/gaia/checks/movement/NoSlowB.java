package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** NoSlow (B) - Detects NoSlow via sneaking speed analysis. */
public class NoSlowB extends Check {
    public NoSlowB(GaiaPlugin plugin) { super(plugin, "NoSlow", "B", "noslow", true, 8); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
