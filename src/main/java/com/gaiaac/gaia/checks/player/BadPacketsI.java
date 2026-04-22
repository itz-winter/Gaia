package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (I) - Detects sprinting backwards. */
public class BadPacketsI extends Check {
    public BadPacketsI(GaiaPlugin plugin) { super(plugin, "BadPackets", "I", "badpackets", true, 5); }
    @Override public void handle(Player player, PlayerData data) {
        if (!data.isSprinting() || data.getDeltaXZ() < 0.1) return;
        // Sprint-backwards is only physically meaningful on the ground. While airborne, sprint
        // momentum carries the player forward even if they steer their yaw sideways — this produces
        // false angle divergence that isn't actually impossible movement.
        if (!data.isOnGround()) return;
        // Block interactions (chests, doors, crafting tables) cause the player to turn their view
        // while strafing — this generates false sprinting-backwards patterns for 500ms
        if (System.currentTimeMillis() - data.getLastInteractionTime() < 500) return;
        double moveAngle = Math.toDegrees(Math.atan2(-data.getDeltaX(), data.getDeltaZ()));
        double yaw = data.getYaw();
        double diff = Math.abs(((yaw - moveAngle) % 360 + 540) % 360 - 180);
        if (diff > 90) {
            double buffer = data.addBuffer("bp_i_buffer", 1);
            if (buffer > 5) { flag(player, data, "sprintBack angle=" + String.format("%.1f", diff)); data.setBuffer("bp_i_buffer", 0); }
        } else { data.decreaseBuffer("bp_i_buffer", 0.5); }
    }
}
