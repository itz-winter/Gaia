package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Invalid (E) - Detects impossible vehicle movement states. */
public class InvalidE extends Check {
    public InvalidE(GaiaPlugin plugin) { super(plugin, "Invalid", "E", "invalid", true, 5); }
    @Override public void handle(Player player, PlayerData data) {
        if (data.isInVehicle() && data.getDeltaXZ() > 2.0 && !data.isGliding()) {
            flag(player, data, "vehicleSpeed dXZ=" + String.format("%.3f", data.getDeltaXZ()));
        }
    }
}
