package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Reach (A) - Detects attacks from beyond maximum legitimate reach.
 * Maximum survival reach is 3.0 blocks. Creative is 4.5.
 * Uses entity ID from INTERACT_ENTITY packet for O(1) lookup via world entity list.
 */
public class ReachA extends Check {

    private static final double MAX_REACH = 3.1; // Slight tolerance over vanilla 3.0

    public ReachA(GaiaPlugin plugin) {
        super(plugin, "Reach", "A", "reach", true, 10);
    }

    @Override
    public void handle(Player player, PlayerData data) {
        if (recentlyTeleported(data) || recentlyReceivedVelocity(data)) return;

        long now = System.currentTimeMillis();
        if (now - data.getLastAttackTime() > 100) return;

        int targetEntityId = data.getLastTargetEntityId();
        if (targetEntityId == -1) return;

        // Find the entity by ID from nearby entities — try-catch for thread safety
        Entity target = null;
        try {
            List<Entity> nearby = player.getNearbyEntities(6, 6, 6);
            for (int i = 0, sz = nearby.size(); i < sz; i++) {
                if (nearby.get(i).getEntityId() == targetEntityId) {
                    target = nearby.get(i);
                    break;
                }
            }
        } catch (Exception ignored) {
            return; // Can't safely read entity list off main thread
        }

        if (target == null) return;

        // Calculate distance — try-catch for thread safety on target.getLocation()
        double distance;
        try {
            double dx = data.getX() - target.getLocation().getX();
            double dz = data.getZ() - target.getLocation().getZ();
            // Use eye height for more accurate reach calculation
            double dy = (data.getY() + 1.62) - (target.getLocation().getY() + (target.getHeight() / 2.0));
            distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            // Subtract hitbox radius (0.3 for most entities)
            distance -= 0.3;
        } catch (Exception ignored) {
            return;
        }

        // Lag compensation — generous for high ping
        double compensatedReach = MAX_REACH + Math.min(data.getPing() / 250.0, 0.8);

        if (distance > compensatedReach) {
            double buffer = data.addBuffer("reach_a_buffer", 1);
            if (buffer > 3) {
                flag(player, data, 2.0, "reach=" + String.format("%.2f", distance)
                        + " max=" + String.format("%.2f", compensatedReach)
                        + " ping=" + data.getPing());
                data.setBuffer("reach_a_buffer", 1);
            }
        } else {
            data.decreaseBuffer("reach_a_buffer", 0.25);
        }
    }
}
