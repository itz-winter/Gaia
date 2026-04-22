package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Step (A) - Detects unnatural step height (> 0.6 blocks). */
public class StepA extends Check {
    public StepA(GaiaPlugin plugin) { super(plugin, "Step", "A", "step", true, 8); }
    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data) || data.isFlying() || data.isGliding() || data.isWearingElytra() || data.isInVehicle() || recentlyReceivedVelocity(data)) return;
        // Sanity check: skip if horizontal movement is absurd (position correction / chunk load teleport)
        if (data.getDeltaXZ() > 2.0) return;
        if (data.isOnGround() && data.wasOnGround() && data.getDeltaY() > data.getStepHeightAttribute() + 0.01) {
            flag(player, data, 2.0, "stepHeight=" + String.format("%.4f", data.getDeltaY())
                    + " maxStep=" + String.format("%.2f", data.getStepHeightAttribute()));
        }
    }
}
