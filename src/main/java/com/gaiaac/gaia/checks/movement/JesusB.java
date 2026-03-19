package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Jesus (B) - Detects standing on water surface. */
public class JesusB extends Check {
    public JesusB(GaiaPlugin plugin) { super(plugin, "Jesus", "B", "jesus", true, 8); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
