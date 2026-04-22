package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/**
 * Motion (D) - Detects phase/no-clip movement (blink).
 * Blink cheats teleport the player forward by many blocks in a single position packet.
 * Legitimate movement can't produce dXZ > 5.0 without a server velocity packet or teleport.
 */
public class MotionD extends Check {
    public MotionD(GaiaPlugin plugin) { super(plugin, "Motion", "D", "motion", true, 10); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || data.isFlying() || data.isInVehicle() || data.isGliding()) return;
        if (recentlyReceivedVelocity(data) || data.isWearingElytra()) return;
        if (recentlyJoined(data)) return;
        if (data.isRiptiding()) return;

        double dXZ = data.getDeltaXZ();
        // 5.0 b/t horizontal: impossible from player input alone, even with max speed attribute
        if (dXZ > 5.0 && !data.isInWater() && !data.isInLava()) {
            flag(player, data, 5.0, "blink dXZ=" + String.format("%.4f", dXZ));
        }
    }
}
