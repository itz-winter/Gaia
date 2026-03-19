package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Motion (D) - Detects phase/no-clip movement. */
public class MotionD extends Check {
    public MotionD(GaiaPlugin plugin) { super(plugin, "Motion", "D", "motion", true, 10); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
