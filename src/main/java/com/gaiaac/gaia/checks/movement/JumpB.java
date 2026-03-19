package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Jump (B) - Detects extra jumps (multi-jump). */
public class JumpB extends Check {
    public JumpB(GaiaPlugin plugin) { super(plugin, "Jump", "B", "jump", true, 8); }
    @Override public void handle(Player player, PlayerData data) { }
    @Override public boolean isImplemented() { return false; }
}
