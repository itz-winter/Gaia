package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Strafe (A) - Detects abnormal sideways acceleration. */
public class StrafeA extends Check {
    public StrafeA(GaiaPlugin plugin) { super(plugin, "Strafe", "A", "strafe", true, 8); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
