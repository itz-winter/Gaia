package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (B) - Detects impossible movement flags. */
public class BadPacketsB extends Check {
    public BadPacketsB(GaiaPlugin plugin) { super(plugin, "BadPackets", "B", "badpackets", true, 5); }
    @Override
    public void handle(Player player, PlayerData data) {
        // Sprinting while sneaking is impossible in vanilla
        if (data.isSprinting() && data.isSneaking()) {
            flag(player, data, 3.0, "sprintWhileSneak");
        }
    }
}
