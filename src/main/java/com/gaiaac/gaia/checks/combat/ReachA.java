package com.gaiaac.gaia.checks.combat;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Reach (A) - Detects attacks from beyond maximum legitimate reach.
 * Maximum survival reach is 3.0 blocks. Creative is 4.5.
 * Uses scheduled main-thread entity lookup to avoid calling Bukkit API from netty thread.
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
        if (now - data.getLastAttackTime() > 200) return; // Increased from 100ms — scheduled tasks can arrive late

        int targetEntityId = data.getLastTargetEntityId();
        if (targetEntityId == -1) return;

        // Capture player position at time of attack (from PlayerData — safe to read on netty thread)
        final double attackX = data.getX();
        final double attackY = data.getY();
        final double attackZ = data.getZ();
        final int ping = data.getPing();

        // Schedule entity lookup on the main thread — Bukkit API is NOT thread-safe
        Bukkit.getScheduler().runTask(plugin, () -> {
            Entity target = null;
            try {
                // Use getNearbyEntities on main thread — safe and much faster than getWorld().getEntities()
                for (Entity entity : player.getNearbyEntities(6, 6, 6)) {
                    if (entity.getEntityId() == targetEntityId) {
                        target = entity;
                        break;
                    }
                }
            } catch (Exception ignored) {
                return;
            }

            if (target == null) return;

            double dx = attackX - target.getLocation().getX();
            double dz = attackZ - target.getLocation().getZ();
            double dy = (attackY + 1.62) - (target.getLocation().getY() + (target.getHeight() / 2.0));
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            distance -= 0.3; // Subtract hitbox radius

            // Lag compensation — generous for high ping
            double compensatedReach = MAX_REACH + Math.min(ping / 250.0, 0.8);

            if (distance > compensatedReach) {
                double buffer = data.addBuffer("reach_a_buffer", 1);
                if (buffer > 3) {
                    flag(player, data, 2.0, "reach=" + String.format("%.2f", distance)
                            + " max=" + String.format("%.2f", compensatedReach)
                            + " ping=" + ping);
                    data.setBuffer("reach_a_buffer", 1);
                }
            } else {
                data.decreaseBuffer("reach_a_buffer", 0.25);
            }
        });
    }
}
