package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/**
 * Step (B) - Detects step without proper jump arc (airborne-to-ground step).
 * Normal step-up (StepA) catches onGround→onGround with dY > 0.6.
 * StepB catches: player was AIRBORNE (wasOnGround=false) but lands with dY > 0.6 —
 * meaning they stepped up a block while falling from air, which is impossible in vanilla.
 */
public class StepB extends Check {
    public StepB(GaiaPlugin plugin) { super(plugin, "Step", "B", "step", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data) || data.isFlying() || data.isGliding()) return;
        if (data.isInVehicle() || recentlyReceivedVelocity(data)) return;
        if (data.getDeltaXZ() > 2.0) return; // Sanity check

        // Was airborne last tick, now on ground, but dY > 0.6 (stepped up while landing)
        if (!data.wasOnGround() && data.isOnGround() && data.getDeltaY() > data.getStepHeightAttribute() + 0.01) {
            flag(player, data, 2.0, "airStep stepHeight=" + String.format("%.4f", data.getDeltaY())
                    + " prevAirTicks=" + (data.getAirTicks() + 1));
        }
    }
}
