package com.gaiaac.gaia.checks.combat;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Aim (G) - Reserved for future use. */
public class AimG extends Check {
    public AimG(GaiaPlugin plugin) { super(plugin, "Aim", "G", "aim", false, 10); }
    @Override public void handle(Player player, PlayerData data) { }
}
