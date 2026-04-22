package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/**
 * Scaffold (C) - Detects downward placement patterns (godbridge).
 * Godbridge scaffold: placing blocks below while looking nearly straight down (pitch > 75°)
 * and moving, combined with very fast placement timing.
 */
public class ScaffoldC extends Check {
    public ScaffoldC(GaiaPlugin plugin) { super(plugin, "Scaffold", "C", "scaffoldtimed", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || data.isFlying() || data.isInVehicle()) return;
        if (isBedrockPlayer(data)) return;

        float pitch = data.getPitch();
        double dXZ = data.getDeltaXZ();
        long now = System.currentTimeMillis();
        long lastPlace = data.getLastActualBlockPlaceTime(); // Use confirmed placements only — not bare right-clicks

        // Godbridge: extreme downward look + fast movement + rapid confirmed placement.
        // Threshold raised from 75° to 80° to avoid FPs during normal downward bridging.
        if (pitch > 80f && dXZ > 0.2 && lastPlace > 0 && (now - lastPlace) < 60) {
            double buffer = data.addBuffer("scaffold_c_buf", 1);
            if (buffer > 4) {
                flag(player, data, 1.0, "godbridge pitch=" + String.format("%.2f", pitch)
                        + " dXZ=" + String.format("%.4f", dXZ)
                        + " interval=" + (now - lastPlace) + "ms");
                data.setBuffer("scaffold_c_buf", 2);
            }
        } else {
            data.decreaseBuffer("scaffold_c_buf", 0.5);
        }
    }
}
