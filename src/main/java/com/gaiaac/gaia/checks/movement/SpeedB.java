package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/**
 * Speed (B) - Detects BunnyHop / air speed exploit.
 * In the first few ticks after leaving the ground (bhop window), horizontal speed
 * should be bounded by sprint momentum. Bhop cheats maintain or increase speed
 * beyond what physics allows.
 */
public class SpeedB extends Check {
    public SpeedB(GaiaPlugin plugin) { super(plugin, "Speed", "B", "speed", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || data.isFlying() || data.isInVehicle() || data.isGliding()) return;
        if (data.isInWater() || data.isInLava() || data.isOnClimbable() || recentlyReceivedVelocity(data)) return;
        if (data.isRiptiding()) return;
        if (data.isOnGround() || data.isOnSlime()) return;

        int airTicks = data.getAirTicks();
        if (airTicks < 1 || airTicks > 4) return; // bhop window: first 4 air ticks

        double dXZ = data.getDeltaXZ();
        double speedScale = data.getMovementSpeedAttribute() / 0.1;
        // Raised from 0.42 to 0.47: accounts for ice/slime momentum carried into the jump,
        // and slight variance from the attribute caching 4-tick delay.
        double maxSpeed = 0.47 * speedScale + (data.getPing() / 1000.0);

        if (dXZ > maxSpeed) {
            double buffer = data.addBuffer("speed_b_buf", 1);
            if (buffer > 4) {
                flag(player, data, 1.5, "bhop dXZ=" + String.format("%.4f", dXZ)
                        + " max=" + String.format("%.4f", maxSpeed) + " airTicks=" + airTicks);
                data.setBuffer("speed_b_buf", 2);
            }
        } else {
            data.decreaseBuffer("speed_b_buf", 0.25);
        }
    }
}
