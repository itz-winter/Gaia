package com.gaiaac.gaia.listeners;

import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

/**
 * Bukkit event listener for player state tracking.
 * Manages PlayerData lifecycle and state updates from Bukkit events.
 */
public class PlayerDataListener implements Listener {

    private final GaiaPlugin plugin;

    public PlayerDataListener(GaiaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // PlayerDataManager.getPlayerData() auto-creates via computeIfAbsent
        plugin.getPlayerDataManager().getPlayerData(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getPlayerDataManager().removePlayerData(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        // Reset movement state on world change to prevent false positives
        data.setLastTeleportTime(System.currentTimeMillis());
        data.handleMovement(player.getLocation().getX(), player.getLocation().getY(),
                player.getLocation().getZ(), player.getLocation().getYaw(),
                player.getLocation().getPitch(), true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        data.setLastTeleportTime(System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onToggleSprint(PlayerToggleSprintEvent event) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(event.getPlayer());
        if (data != null) {
            data.setSprinting(event.isSprinting());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(event.getPlayer());
        if (data != null) {
            data.setSneaking(event.isSneaking());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(event.getPlayer());
        if (data != null) {
            data.setFlying(event.isFlying());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        if (event.isCancelled()) return;
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(event.getPlayer());
        if (data != null) {
            data.setGameMode(event.getNewGameMode());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player attacker = (Player) event.getDamager();
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(attacker);
        if (data != null) {
            data.setLastAttackTime(System.currentTimeMillis());
        }
    }
}
