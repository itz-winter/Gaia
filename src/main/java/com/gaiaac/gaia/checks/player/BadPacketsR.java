package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (R) - Detects extremely high rotation delta (impossible head snap). */
public class BadPacketsR extends Check {
    public BadPacketsR(GaiaPlugin plugin) { super(plugin, "BadPackets", "R", "badpackets", true, 3); }
    @Override public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;
        double totalRot = Math.sqrt(data.getDeltaYaw() * data.getDeltaYaw() + data.getDeltaPitch() * data.getDeltaPitch());
        if (totalRot > 300) {
            flag(player, data, 3.0, "extremeRotation total=" + String.format("%.1f", totalRot));
        }
    }
}
