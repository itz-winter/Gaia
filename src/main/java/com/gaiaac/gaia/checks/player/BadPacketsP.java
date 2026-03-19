package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** BadPackets (P) - Detects placing blocks and attacking simultaneously. */
public class BadPacketsP extends Check {
    public BadPacketsP(GaiaPlugin plugin) { super(plugin, "BadPackets", "P", "badpackets", true, 5); }
    @Override public void handle(Player player, PlayerData data) {
        long now = System.currentTimeMillis();
        if (now - data.getLastAttackTime() < 50 && now - data.getLastBlockPlaceTime() < 50) {
            double buffer = data.addBuffer("bp_p_buffer", 1);
            if (buffer > 5) { flag(player, data, "simultaneousAction"); data.setBuffer("bp_p_buffer", 0); }
        } else { data.decreaseBuffer("bp_p_buffer", 0.5); }
    }
}
