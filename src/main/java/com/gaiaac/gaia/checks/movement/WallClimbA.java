package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** WallClimb (A) - Detects upward movement along vertical surfaces (spider hack). */
public class WallClimbA extends Check {
    public WallClimbA(GaiaPlugin plugin) { super(plugin, "WallClimb", "A", "wallclimb", true, 5); }
    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || data.isFlying() || data.isInVehicle() || data.isOnClimbable()
                || data.isInWater() || data.isInLava() || data.isSwimming() || recentlyReceivedVelocity(data)) return;
        if (data.hasLevitation() || data.isRiptiding() || data.isInBubbleColumn()) return;
        // Ascending without being on climbable, not jumping (multiple ticks up)
        if (data.getDeltaY() > 0.1 && data.getAirTicks() > 5 && !data.isGliding()) {
            double buffer = data.addBuffer("wallclimb_a_buffer", 1);
            if (buffer > 5) {
                flag(player, data, 2.0, "wallClimb dY=" + String.format("%.4f", data.getDeltaY()) + " airTicks=" + data.getAirTicks());
                data.setBuffer("wallclimb_a_buffer", 0);
            }
        } else {
            data.decreaseBuffer("wallclimb_a_buffer", 0.5);
        }
    }
}
