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
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

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
        // Clean up any debug watches for or by this player
        plugin.getAlertManager().removeAllWatches(event.getPlayer().getUniqueId());
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
            // Track when sneaking STARTS — NoSlowB needs a grace period for deceleration
            if (event.isSneaking()) data.setLastSneakToggleTime(System.currentTimeMillis());
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
    public void onToggleGlide(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        if (data != null) {
            data.setGliding(event.isGliding());
            // Also update wearingElytra — if gliding starts, they definitely have elytra
            if (event.isGliding()) {
                data.setWearingElytra(true);
            }
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

    // === Teleport-like events that do NOT fire PlayerTeleportEvent ===
    // All set lastTeleportTime so VClip/movement checks don't flag the position jump.

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(event.getPlayer());
        if (data != null) data.setLastTeleportTime(System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBedEnter(PlayerBedEnterEvent event) {
        // Bed entry teleports the player to the bed block — does not fire PlayerTeleportEvent
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(event.getPlayer());
        if (data != null) data.setLastTeleportTime(System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBedLeave(PlayerBedLeaveEvent event) {
        // Waking up also repositions the player
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(event.getPlayer());
        if (data != null) data.setLastTeleportTime(System.currentTimeMillis());
    }

    // === Vehicle exit — set grace so BadPackets checks don't fire on GSit stand-up teleport ===

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVehicleExit(VehicleExitEvent event) {
        if (!(event.getExited() instanceof Player)) return;
        Player player = (Player) event.getExited();
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        if (data == null) return;
        long now = System.currentTimeMillis();
        // GSit (and similar plugins) teleport the player when they stand up from a seat.
        // Set both lastVehicleExitTime (BadPackets rotation/position grace) and lastTeleportTime
        // (VClip/movement checks) so the sudden pitch snap + position jump isn't flagged.
        data.setLastVehicleExitTime(now);
        data.setLastTeleportTime(now);
    }

    /**
     * Track confirmed block placements (not just right-click interactions).     * PLAYER_BLOCK_PLACEMENT fires for any right-click on a block (chests, buttons, doors, etc.),
     * so scaffold checks that analyse sustained movement must gate on actual placements only.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(event.getPlayer());
        if (data != null) data.setLastActualBlockPlaceTime(System.currentTimeMillis());
    }
}
