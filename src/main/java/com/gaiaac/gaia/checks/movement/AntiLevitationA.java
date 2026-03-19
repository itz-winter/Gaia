package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

/** AntiLevitation (A) - Detects levitation effect bypass. */
public class AntiLevitationA extends Check {
    public AntiLevitationA(GaiaPlugin plugin) { super(plugin, "AntiLevitation", "A", "antilevitation", true, 5); }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || data.isFlying() || data.isGliding()) return;

        try {
            boolean hasLevitation = player.hasPotionEffect(PotionEffectType.LEVITATION);
            if (hasLevitation && data.getDeltaY() < -0.1 && data.getAirTicks() > 5) {
                flag(player, data, 2.0, "levitationBypass dY=" + String.format("%.4f", data.getDeltaY()));
            }
        } catch (Exception ignored) {}
    }
}
