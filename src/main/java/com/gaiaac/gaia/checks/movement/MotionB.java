package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Motion (B) - Detects invalid vertical acceleration. */
public class MotionB extends Check {
    public MotionB(GaiaPlugin plugin) { super(plugin, "Motion", "B", "motion", true, 10); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
