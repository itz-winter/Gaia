package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Speed (C) - Detects ice super-speed exploit. */
public class SpeedC extends Check {
    public SpeedC(GaiaPlugin plugin) { super(plugin, "Speed", "C", "speed", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (!data.isOnIce() || !data.isOnGround()) return;
        if (recentlyTeleported(data) || data.isFlying() || data.isInVehicle() || recentlyReceivedVelocity(data)) return;

        double dXZ = data.getDeltaXZ();
        double speedScale = data.getMovementSpeedAttribute() / 0.1;
        // Blue ice absolute max with active sprint: ~3.0 * speedScale
        double maxIceSpeed = 3.0 * speedScale + (data.getPing() / 1000.0);

        if (dXZ > maxIceSpeed) {
            double buffer = data.addBuffer("speed_c_buf", 1);
            if (buffer > 2) {
                flag(player, data, 2.0, "iceSpeed dXZ=" + String.format("%.4f", dXZ)
                        + " max=" + String.format("%.4f", maxIceSpeed));
                data.setBuffer("speed_c_buf", 0);
            }
        } else {
            data.decreaseBuffer("speed_c_buf", 0.25);
        }
    }
}
