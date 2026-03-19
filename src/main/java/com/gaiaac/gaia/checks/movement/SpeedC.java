package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Speed (C) - Detects speed on ice. */
public class SpeedC extends Check {
    public SpeedC(GaiaPlugin plugin) { super(plugin, "Speed", "C", "speed", true, 10); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
