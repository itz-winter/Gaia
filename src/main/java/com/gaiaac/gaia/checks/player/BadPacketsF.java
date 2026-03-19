package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (F) - Detects NaN/Infinity position values. */
public class BadPacketsF extends Check {
    public BadPacketsF(GaiaPlugin plugin) { super(plugin, "BadPackets", "F", "badpackets", true, 3); }
    @Override
    public void handle(Player player, PlayerData data) {
        if (Double.isNaN(data.getX()) || Double.isInfinite(data.getX())
                || Double.isNaN(data.getY()) || Double.isInfinite(data.getY())
                || Double.isNaN(data.getZ()) || Double.isInfinite(data.getZ())) {
            flag(player, data, 10.0, "invalidPosition NaN/Infinity");
        }
    }
}
