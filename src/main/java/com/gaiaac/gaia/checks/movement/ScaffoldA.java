package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Scaffold (A) - Detects automated block placement timing. */
public class ScaffoldA extends Check {
    public ScaffoldA(GaiaPlugin plugin) { super(plugin, "Scaffold", "A", "scaffold", true, 8); }
    @Override public boolean isImplemented() { return false; }
    @Override
    public void handle(Player player, PlayerData data) {
        // Constant placement timing detection
    }
}
