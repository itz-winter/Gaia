package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** Criticals (B) - Detects ground spoofing for critical hits via Y-position analysis. */
public class CriticalsB extends Check {
    public CriticalsB(GaiaPlugin plugin) { super(plugin, "Criticals", "B", "criticals", true, 5); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyReceivedVelocity(data)) return;
        long now = System.currentTimeMillis();
        if (now - data.getLastAttackTime() > 100) return;

        // Micro-jump criticals: tiny Y offsets repeated on attacks
        double dY = data.getDeltaY();
        if (!data.isOnGround() && dY > 0 && dY < 0.0625 && data.getAirTicks() <= 1) {
            double buffer = data.addBuffer("criticals_b_buffer", 1);
            if (buffer > 3) {
                flag(player, data, 2.0, "microJumpCrit dY=" + dY + " airTicks=" + data.getAirTicks());
                data.setBuffer("criticals_b_buffer", 0);
            }
        } else {
            data.decreaseBuffer("criticals_b_buffer", 0.5);
        }
    }
}
