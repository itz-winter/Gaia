package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Motion (C) - Detects invalid horizontal deceleration. */
public class MotionC extends Check {
    public MotionC(GaiaPlugin plugin) { super(plugin, "Motion", "C", "motion", true, 10); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
