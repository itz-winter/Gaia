package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/**
 * Aim (O) - Detects extremely high rotation speed (spin aura / fast aim).
 */
public class AimO extends Check {
    public AimO(GaiaPlugin plugin) { super(plugin, "Aim", "O", "aim", true, 6); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyJoined(data)) return;

        float deltaYaw = data.getDeltaYaw();
        float deltaPitch = data.getDeltaPitch();
        double totalDelta = Math.sqrt(deltaYaw * deltaYaw + deltaPitch * deltaPitch);

        if (totalDelta > 180.0) {
            flag(player, data, "spinAim total=" + String.format("%.2f", totalDelta)
                    + " dYaw=" + deltaYaw + " dPitch=" + deltaPitch);
        }
    }
}
