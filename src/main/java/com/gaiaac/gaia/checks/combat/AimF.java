package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Aim (F) - Detects GCD bypass (sensitivity-based rotation analysis).
 * Minecraft rotations are quantized by mouse sensitivity. Modified clients bypass this.
 * Uses the Minecraft sensitivity formula: f = sens * 0.6 + 0.2, delta = f^3 * 1.2
 */
public class AimF extends Check {

    private static final double MIN_GCD = 0.00001;

    public AimF(GaiaPlugin plugin) {
        super(plugin, "Aim", "F", "aim", true, 15);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        float deltaYaw = data.getDeltaYaw();
        float deltaPitch = data.getDeltaPitch();

        if (deltaYaw < 0.5f && deltaPitch < 0.5f) return;
        if (data.getLastDeltaYaw() < 0.5f && data.getLastDeltaPitch() < 0.5f) return;

        double yawGcd = gcd(deltaYaw, data.getLastDeltaYaw());
        double pitchGcd = gcd(deltaPitch, data.getLastDeltaPitch());

        boolean yawBypass = yawGcd < MIN_GCD && deltaYaw > 1.5f;
        boolean pitchBypass = pitchGcd < MIN_GCD && deltaPitch > 1.5f;

        if (yawBypass || pitchBypass) {
            double buffer = data.addBuffer("aim_f_buffer", 1);
            if (buffer > 7) {
                flag(player, data, 1.5, "gcdBypass yGcd=" + String.format("%.8f", yawGcd)
                        + " pGcd=" + String.format("%.8f", pitchGcd)
                        + " dYaw=" + String.format("%.4f", deltaYaw)
                        + " dPitch=" + String.format("%.4f", deltaPitch));
                data.setBuffer("aim_f_buffer", 3);
            }
        } else {
            data.decreaseBuffer("aim_f_buffer", 0.5);
        }
    }

    private double gcd(double a, double b) {
        a = Math.abs(a);
        b = Math.abs(b);
        for (int i = 0; i < 50 && b > 1E-8; i++) {
            double temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
}
