package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Step (C) - Detects step while moving at high speed. */
public class StepC extends Check {
    public StepC(GaiaPlugin plugin) { super(plugin, "Step", "C", "step", true, 8); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
