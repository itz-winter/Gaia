package com.gaiaac.gaia.checks.movement;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Sprint (A) - Detects sprint state inconsistencies (sprinting backwards, while eating, etc.). */
public class SprintA extends Check {
    public SprintA(GaiaPlugin plugin) { super(plugin, "Sprint", "A", "sprint", true, 5); }
    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || data.isFlying()) return;
        // Sprinting while using item (eating/blocking)
        if (data.isSprinting() && (player.isBlocking() || player.getItemInUse() != null)) {
            flag(player, data, 1.0, "sprintWhileUsing");
        }
    }
}
