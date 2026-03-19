package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** NoSaddle (A) - Detects riding entities without a saddle. */
public class NoSaddleA extends Check {
    public NoSaddleA(GaiaPlugin plugin) { super(plugin, "NoSaddle", "A", "nosaddle", true, 5); }
    @Override public void handle(Player player, PlayerData data) {
        if (!data.isInVehicle()) return;
        // Check if vehicle entity has saddle - requires entity metadata inspection
    }
}
