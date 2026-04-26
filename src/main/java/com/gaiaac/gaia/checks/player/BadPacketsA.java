package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** BadPackets (A) - Detects invalid pitch values (> 90 or < -90). */
public class BadPacketsA extends Check {
    public BadPacketsA(GaiaPlugin plugin) { super(plugin, "BadPackets", "A", "badpackets", true, 5); }
    @Override
    public void handle(Player player, PlayerData data) {
        // GSit / plugin seats: player's pitch may be forced outside normal range while seated
        if (data.isInVehicle() || System.currentTimeMillis() - data.getLastVehicleExitTime() < 500) return;
        float pitch = data.getPitch();
        if (Math.abs(pitch) > 90.0f) {
            flag(player, data, 5.0, "invalidPitch=" + pitch);
        }
    }
}
