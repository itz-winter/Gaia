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
        if (recentlyTeleported(data) || data.isFlying() || data.isInVehicle()) return;
        double dY = Math.abs(data.getDeltaY());
        if (dY > 3.0 && !recentlyReceivedVelocity(data)) {
            flag(player, data, 5.0, "vclip dY=" + String.format("%.4f", data.getDeltaY()));
        }
    }
}
