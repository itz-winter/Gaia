package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** VClip (A) - Detects vertical teleportation (phase through blocks). */
public class VClipA extends Check {
    public VClipA(GaiaPlugin plugin) { super(plugin, "VClip", "A", "vclip", true, 5); }
    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data) || data.isFlying() || data.isInVehicle()) return;
        if (recentlyReceivedVelocity(data)) return;
        if (data.isRiptiding() || data.isInBubbleColumn()) return;
        double dY = Math.abs(data.getDeltaY());
        // > 3.0 blocks vertical in one tick is impossible without teleportation or velocity.
        // Ender pearl, chorus fruit, bed enter, respawn all set recentlyTeleported above.
        if (dY > 3.0) {
            flag(player, data, 5.0, "vclip dY=" + String.format("%.4f", data.getDeltaY()));
        }
    }
}
