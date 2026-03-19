package com.gaiaac.gaia.checks.movement;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;

/** EntitySpeed (A) - Detects modified riding speeds. */
public class EntitySpeedA extends Check {
    public EntitySpeedA(GaiaPlugin plugin) { super(plugin, "EntitySpeed", "A", "entityspeed", true, 8); }
    @Override
    public void handle(Player player, PlayerData data) {
        if (!data.isInVehicle() || recentlyTeleported(data)) return;
        if (data.getDeltaXZ() > 1.5) {
            flag(player, data, 1.0, "entitySpeed dXZ=" + String.format("%.4f", data.getDeltaXZ()));
        }
    }
}
