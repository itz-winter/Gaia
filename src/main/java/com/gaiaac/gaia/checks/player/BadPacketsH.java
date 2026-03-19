package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (H) - Detects sprinting while sneaking. */
public class BadPacketsH extends Check {
    public BadPacketsH(GaiaPlugin plugin) { super(plugin, "BadPackets", "H", "badpackets", true, 5); }
    @Override public void handle(Player player, PlayerData data) {
        if (data.isSprinting() && data.isSneaking()) {
            flag(player, data, 2.0, "sprintWhileSneak");
        }
    }
}
