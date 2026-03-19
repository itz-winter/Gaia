package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Scaffold (E) - Detects consistent block placement while moving backwards. */
public class ScaffoldE extends Check {
    public ScaffoldE(GaiaPlugin plugin) { super(plugin, "Scaffold", "E", "scaffold", true, 8); }
    @Override public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;
        long timeSincePlace = System.currentTimeMillis() - data.getLastBlockPlaceTime();
        if (timeSincePlace < 200 && data.getDeltaXZ() > 0.1) {
            float yaw = data.getYaw();
            double moveAngle = Math.toDegrees(Math.atan2(-data.getDeltaX(), data.getDeltaZ()));
            double diff = Math.abs(((yaw - moveAngle) % 360 + 540) % 360 - 180);
            if (diff > 120) {
                double buffer = data.addBuffer("scaffold_e_buffer", 1);
                if (buffer > 6) {
                    flag(player, data, "backwardScaffold angle=" + String.format("%.1f", diff));
                    data.setBuffer("scaffold_e_buffer", 0);
                }
            } else { data.decreaseBuffer("scaffold_e_buffer", 0.5); }
        }
    }
}
