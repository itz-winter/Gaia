package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Aim (A) - Detects rotation snapping.
 * Checks for GCD (Greatest Common Divisor) patterns in rotations
 * that indicate aimbot/aim-assist modifications.
 */
public class AimA extends Check {

    public AimA(GaiaPlugin plugin) {
        super(plugin, "Aim", "A", "aim", true, 10);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        // Only check during combat - must have attacked within the last 2 seconds
        long timeSinceAttack = System.currentTimeMillis() - data.getLastAttackTime();
        if (timeSinceAttack > 2000) return;

        float deltaYaw = data.getDeltaYaw();
        float deltaPitch = data.getDeltaPitch();

        if (deltaYaw < 1.5f || deltaPitch < 1.5f) return;

        // Check for rotation snapping - exact integer rotations
        boolean yawSnapped = deltaYaw % 1.0f == 0;
        boolean pitchSnapped = deltaPitch % 1.0f == 0;

        if (yawSnapped && pitchSnapped) {
            double buffer = data.addBuffer("aim_a_buffer", 1);
            if (buffer > 5) {
                flag(player, data, 1.5, "dYaw=" + deltaYaw + " dPitch=" + deltaPitch + " snapped=true");
                data.setBuffer("aim_a_buffer", 2);
            }
        } else {
            data.decreaseBuffer("aim_a_buffer", 0.5);
        }
    }
}
