package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Speed (D) - Detects speed exploit via prediction comparison. */
public class SpeedD extends Check {
    public SpeedD(GaiaPlugin plugin) { super(plugin, "Speed", "D", "speed", true, 10); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
