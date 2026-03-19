package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Speed (B) - Detects air speed exceeding limits. */
public class SpeedB extends Check {
    public SpeedB(GaiaPlugin plugin) { super(plugin, "Speed", "B", "speed", true, 10); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
