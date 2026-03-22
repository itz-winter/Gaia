package com.gaiaac.gaia.checks.combat;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
public class AimY extends Check {
    public AimY(GaiaPlugin plugin) { super(plugin, "Aim", "Y", "aim", true, 5); }
    @Override public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;
        float pitch = data.getPitch();
        if (pitch > 90.01f || pitch < -90.01f) {
            flag(player, data, 5.0, "pitch=" + String.format("%.4f", pitch));
        }
    }
}
