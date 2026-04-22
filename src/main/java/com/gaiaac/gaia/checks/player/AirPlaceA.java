package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/**
 * AirPlace (A) - Detects placing blocks without adjacent support block.
 * Placing blocks while airborne for many ticks at sprint speed is characteristic of
 * scaffold/airplace cheats that create floating structures mid-air.
 */
public class AirPlaceA extends Check {
    public AirPlaceA(GaiaPlugin plugin) { super(plugin, "AirPlace", "A", "airplace", true, 5); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (data.isFlying() || data.isInVehicle() || data.isGliding()) return;
        if (recentlyTeleported(data) || recentlyReceivedVelocity(data)) return;
        if (data.isInWater() || data.isInLava() || data.isOnClimbable()) return;
        if (data.isOnGround() || data.wasOnGround()) return; // Just left ground — allow

        int airTicks = data.getAirTicks();
        if (airTicks < 4) return; // Allow short jump placements

        // Placing blocks while high in the air at sprint speed = airplace cheat
        if (airTicks > 8 && data.getDeltaXZ() > 0.22) {
            double buffer = data.addBuffer("airplace_a_buf", 1);
            if (buffer > 5) {
                flag(player, data, 1.0, "airPlace airTicks=" + airTicks
                        + " dXZ=" + String.format("%.4f", data.getDeltaXZ()));
                data.setBuffer("airplace_a_buf", 3);
            }
        } else {
            data.decreaseBuffer("airplace_a_buf", 1.0);
        }
    }
}
