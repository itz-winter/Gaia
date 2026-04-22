package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/**
 * Scaffold (B) - Detects rotation lock during consecutive airborne block placements.
 *
 * Real players naturally adjust their aim between placements — even just a fraction of a degree.
 * Scaffold cheats snap to the exact same yaw+pitch every placement (rotation locked), because they
 * calculate the exact required angle programmatically and hold it perfectly still.
 *
 * Detection logic:
 *  - Player places blocks on 4+ consecutive air ticks
 *  - The yaw AND pitch have not changed at all between the last 3 consecutive placements (delta < 0.05°)
 *  - This combination is essentially impossible for a human bridging at speed
 *
 * Bedrock players (Geyser) are excluded — their input model can produce this pattern legitimately.
 */
public class ScaffoldB extends Check {
    public ScaffoldB(GaiaPlugin plugin) { super(plugin, "Scaffold", "B", "scaffoldtimed", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || data.isFlying() || data.isInVehicle()) return;
        if (isBedrockPlayer(data)) return;
        if (data.isOnGround()) return; // only flag airborne placements

        int consecutiveAir = data.getConsecutiveAirPlacements();
        if (consecutiveAir < 3) return; // Need at least 3 consecutive airborne placements

        float currentYaw   = data.getYaw();
        float currentPitch = data.getPitch();
        float lastYaw      = data.getLastBlockPlaceYaw();
        float lastPitch    = data.getLastBlockPlacePitch();

        // Wrap yaw delta to [-180, 180]
        float yawDiff = Math.abs(currentYaw - lastYaw);
        if (yawDiff > 180f) yawDiff = 360f - yawDiff;
        float pitchDiff = Math.abs(currentPitch - lastPitch);

        // Rotation perfectly locked: < 0.05° change across consecutive airborne placements
        boolean rotationLocked = yawDiff < 0.05f && pitchDiff < 0.05f;

        if (rotationLocked) {
            double buffer = data.addBuffer("scaffold_b_buf", 1);
            if (buffer > 4) {
                flag(player, data, 1.0, "rotLock yawDiff=" + String.format("%.3f", yawDiff)
                        + " pitchDiff=" + String.format("%.3f", pitchDiff)
                        + " consecutiveAir=" + consecutiveAir
                        + " dXZ=" + String.format("%.4f", data.getDeltaXZ()));
                data.setBuffer("scaffold_b_buf", 2);
            }
        } else {
            data.decreaseBuffer("scaffold_b_buf", 0.5);
        }
    }
}
