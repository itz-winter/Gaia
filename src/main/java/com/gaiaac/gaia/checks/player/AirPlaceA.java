package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** AirPlace (A) - Detects placing blocks without adjacent support block. */
public class AirPlaceA extends Check {
    public AirPlaceA(GaiaPlugin plugin) { super(plugin, "AirPlace", "A", "airplace", true, 5); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
