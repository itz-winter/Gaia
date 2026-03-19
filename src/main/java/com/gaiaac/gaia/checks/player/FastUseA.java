package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** FastUse (A) - Detects faster-than-normal item usage. */
public class FastUseA extends Check {
    public FastUseA(GaiaPlugin plugin) { super(plugin, "FastUse", "A", "fastuse", true, 5); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
