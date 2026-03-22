package com.gaiaac.gaia.checks.combat;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Aim (H) - Disabled: micro-rotation detection causes too many false positives with high-sensitivity mice. */
public class AimH extends Check {
    public AimH(GaiaPlugin plugin) { super(plugin, "Aim", "H", "aim", false, 12); }
    @Override public void handle(Player player, PlayerData data) { }
}
