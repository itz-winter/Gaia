package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/**
 * Scaffold (D) - Detects scaffold speed anomalies.
 * Placing blocks while moving at sprint speed in mid-air is a scaffold cheat signature.
 * Legitimate air-bridging requires significant slowdown to maintain control.
 */
public class ScaffoldD extends Check {
    public ScaffoldD(GaiaPlugin plugin) { super(plugin, "Scaffold", "D", "scaffold", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || data.isFlying() || data.isInVehicle()) return;
        if (isBedrockPlayer(data)) return;
        if (data.isInWater() || data.isInLava()) return;
        if (data.isOnGround()) return; // Only flag while airborne

        double dXZ = data.getDeltaXZ();
        double speedScale = data.getMovementSpeedAttribute() / 0.1;
        // Sprint-jump normally reaches 0.35-0.40 b/t in the air.
        // Raised from 0.33 to 0.42 (actual sprint speed ceiling) to avoid FP on normal sprint-jumping.
        // This still catches scaffold cheats that run FASTER than sprint while placing.
        double threshold = 0.42 * speedScale + (data.getPing() / 1000.0);

        if (dXZ > threshold && data.getAirTicks() > 5) {
            double buffer = data.addBuffer("scaffold_d_buf", 1);
            if (buffer > 8) {
                flag(player, data, 1.0, "scaffoldSpeed dXZ=" + String.format("%.4f", dXZ)
                        + " max=" + String.format("%.4f", threshold) + " airTicks=" + data.getAirTicks());
                data.setBuffer("scaffold_d_buf", 4);
            }
        } else {
            data.decreaseBuffer("scaffold_d_buf", 0.5);
        }
    }
}
