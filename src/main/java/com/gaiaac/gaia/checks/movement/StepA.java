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
        if (recentlyTeleported(data) || data.isFlying() || data.isInVehicle() || recentlyReceivedVelocity(data)) return;
        if (data.isOnGround() && data.wasOnGround() && data.getDeltaY() > 0.6) {
            flag(player, data, 2.0, "stepHeight=" + String.format("%.4f", data.getDeltaY()));
        }
    }
}
