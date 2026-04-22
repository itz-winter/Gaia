package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Speed (D) - Detects absolute extreme speed (high-ceiling catch-all). */
public class SpeedD extends Check {
    public SpeedD(GaiaPlugin plugin) { super(plugin, "Speed", "D", "speed", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || data.isFlying() || data.isInVehicle() || data.isGliding()) return;
        if (data.isInWater() || data.isInLava() || recentlyReceivedVelocity(data)) return;
        if (data.isWearingElytra()) return;
        if (data.isRiptiding()) return;

        double dXZ = data.getDeltaXZ();
        double speedScale = data.getMovementSpeedAttribute() / 0.1;
        // Absolute ceiling: sprint + ice = ~0.36 * 2.5 ≈ 0.9 b/t; non-ice max ≈ 0.5 b/t
        double absMax = (data.isOnIce() ? 1.0 : 0.55) * speedScale + (data.getPing() / 500.0);

        if (dXZ > absMax) {
            flag(player, data, 2.0, "extremeSpeed dXZ=" + String.format("%.4f", dXZ)
                    + " max=" + String.format("%.4f", absMax));
        }
    }
}
