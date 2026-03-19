package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** NoSlow (C) - Detects NoSlow via eating speed. */
public class NoSlowC extends Check {
    public NoSlowC(GaiaPlugin plugin) { super(plugin, "NoSlow", "C", "noslow", true, 8); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
