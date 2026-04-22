package com.gaiaac.gaia.core;

import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.checks.CheckManager;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.*;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.github.retrooper.packetevents.protocol.player.User;
import org.bukkit.entity.Player;

import java.util.List;

public class PacketManager implements PacketListener {

    private final GaiaPlugin plugin;

    public PacketManager(GaiaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        User user = event.getUser();
        if (user == null || user.getUUID() == null) return;

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(user.getUUID());
        if (data == null) return;

        // Use cached permission + gamemode — no Bukkit API call on netty thread
        if (data.isExempt()) return;

        Player player = data.getPlayer();
        if (player == null) return;

        CheckManager cm = plugin.getCheckManager();

        // Record packet timestamp (for timer checks)
        data.addPacketTimestamp();

        // === Movement position packets ===
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION) {
            WrapperPlayClientPlayerPosition wrapper = new WrapperPlayClientPlayerPosition(event);
            data.handleMovement(wrapper.getPosition().getX(), wrapper.getPosition().getY(),
                    wrapper.getPosition().getZ(), data.getYaw(), data.getPitch(), wrapper.isOnGround());
            runChecks(cm.getMovementChecks(), player, data);

        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            WrapperPlayClientPlayerPositionAndRotation wrapper = new WrapperPlayClientPlayerPositionAndRotation(event);
            data.handleMovement(wrapper.getPosition().getX(), wrapper.getPosition().getY(),
                    wrapper.getPosition().getZ(), wrapper.getYaw(), wrapper.getPitch(), wrapper.isOnGround());
            runChecks(cm.getMovementChecks(), player, data);
            // Aim checks run on all rotation packets — skip for 300ms after any right-click interaction
            // (players snap their view to look at what they're clicking, causing false aim patterns).
            // Also skip while sleeping in bed (forced head rotation), in a bubble column (wild movement),
            // or while actively falling (natural quick-looks while airborne are not aimbot behaviour).
            boolean activelyFalling = data.getAirTicks() > 5 && data.getDeltaY() < -0.2;
            if (System.currentTimeMillis() - data.getLastInteractionTime() > 300
                    && !data.isSleeping() && !data.isInBubbleColumn() && !activelyFalling) {
                runChecks(cm.getCombatChecksByCategory("aim"), player, data);
            }

        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION) {
            WrapperPlayClientPlayerRotation wrapper = new WrapperPlayClientPlayerRotation(event);
            data.handleMovement(data.getX(), data.getY(), data.getZ(),
                    wrapper.getYaw(), wrapper.getPitch(), wrapper.isOnGround());
            // Aim checks run on all rotation packets — skip for 300ms after any right-click interaction
            boolean activelyFalling2 = data.getAirTicks() > 5 && data.getDeltaY() < -0.2;
            if (System.currentTimeMillis() - data.getLastInteractionTime() > 300
                    && !data.isSleeping() && !data.isInBubbleColumn() && !activelyFalling2) {
                runChecks(cm.getCombatChecksByCategory("aim"), player, data);
            }

        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_FLYING) {
            WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);
            data.handleMovement(data.getX(), data.getY(), data.getZ(),
                    data.getYaw(), data.getPitch(), wrapper.isOnGround());
            // Only badpackets/groundspoof/invalid on flying-only packets
            runChecks(cm.getPlayerChecksByCategory("badpackets"), player, data);
            runChecks(cm.getPlayerChecksByCategory("groundspoof"), player, data);
            runChecks(cm.getPlayerChecksByCategory("invalid"), player, data);

        // === Combat packets ===
        } else if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
            if (wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                long now = System.currentTimeMillis();
                int targetId = wrapper.getEntityId();
                data.setLastAttackTime(now);
                data.addClick();
                // Capture rotation at attack time for FreecamAttack detection (AimH)
                data.setAttackYaw(data.getYaw());
                data.setAttackPitch(data.getPitch());
                // Attacking in vanilla cancels item use (can't eat and attack simultaneously)
                data.setUsingItem(false);

                // Track multi-target attacks for KillAura detection
                int lastId = data.getLastAttackTargetEntityId();
                if (lastId != -1 && lastId != targetId) {
                    long timeSinceSwitch = now - data.getLastAttackTargetSwitchTime();
                    if (timeSinceSwitch < 100) { // switched targets within 100ms
                        data.setAttackTargetCount(data.getAttackTargetCount() + 1);
                    } else {
                        data.setAttackTargetCount(1);
                    }
                    data.setLastAttackTargetSwitchTime(now);
                } else if (lastId == -1) {
                    data.setAttackTargetCount(1);
                }
                data.setLastAttackTargetEntityId(targetId);
                data.setLastTargetEntityId(targetId);

                runChecks(cm.getCombatChecks(), player, data);
            } else {
                // INTERACT or INTERACT_AT (right-clicking a mob/villager/NPC): player often turns their
                // head toward the entity they clicked — guard aim checks for 300ms to prevent FPs.
                data.setLastInteractionTime(System.currentTimeMillis());
            }

        // === Block placement ===
        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            // Right-clicking a block cancels any active item use (eating, blocking, bow draw).
            // Without this, USE_ITEM + PLAYER_BLOCK_PLACEMENT arriving together (right-click on block
            // while holding food/bow) leaves isUsingItem=true and triggers NoSlowA FP.
            data.setUsingItem(false);
            // Any right-click interaction: guard aim checks for 300ms (players snap view to target)
            data.setLastInteractionTime(System.currentTimeMillis());
            // Run checks BEFORE updating timestamp — checks need to compare against the PREVIOUS placement time
            runChecks(cm.getPlayerChecksByCategory("fastplace"), player, data);
            // Only run packet-timing scaffold checks (A/B/C/F) on block placement packets.
            // Movement-based scaffold checks (E/G/H/I/J/K/L/M/N/O) run on movement packets only —
            // running them here caused FPs when right-clicking within a recent placement window.
            runChecks(cm.getMovementChecksByCategory("scaffoldtimed"), player, data);
            runChecks(cm.getPlayerChecksByCategory("airplace"), player, data);
            runChecks(cm.getPlayerChecksByCategory("tower"), player, data);
            // Track placement rotation and consecutive airborne placements for ScaffoldB
            if (!data.isOnGround()) {
                data.setConsecutiveAirPlacements(data.getConsecutiveAirPlacements() + 1);
            } else {
                data.setConsecutiveAirPlacements(0);
            }
            data.setLastBlockPlaceYaw(data.getYaw());
            data.setLastBlockPlacePitch(data.getPitch());
            data.setLastBlockPlaceTime(System.currentTimeMillis());

        // === Digging ===
        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);
            if (wrapper.getAction() == com.github.retrooper.packetevents.protocol.player.DiggingAction.RELEASE_USE_ITEM) {
                data.setUsingItem(false);
            }

            // Map PacketEvents DiggingAction to our int codes for FastBreak
            com.github.retrooper.packetevents.protocol.player.DiggingAction digAction = wrapper.getAction();
            if (digAction == com.github.retrooper.packetevents.protocol.player.DiggingAction.START_DIGGING) {
                data.setDiggingAction(0);
                data.setCurrentlyDigging(true);
                // Look up the block on the main thread (Bukkit API not safe on netty thread)
                final int bx = wrapper.getBlockPosition().getX();
                final int by = wrapper.getBlockPosition().getY();
                final int bz = wrapper.getBlockPosition().getZ();
                // Save target block coords immediately (netty-safe) for FreecamDigA check
                data.setLastDigTarget(bx, by, bz);
                // Capture rotation at dig time for FreecamDig detection (AimI)
                data.setAttackYaw(data.getYaw());
                data.setAttackPitch(data.getPitch());
                org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        org.bukkit.block.Block block = player.getWorld().getBlockAt(bx, by, bz);
                        data.setLastDigBlock(block);
                    } catch (Exception ignored) {}
                });
                // Run freecam-dig check (AimI) after capturing target + rotation
                runChecks(cm.getCombatChecksByCategory("freecamdig"), player, data);
            } else if (digAction == com.github.retrooper.packetevents.protocol.player.DiggingAction.CANCELLED_DIGGING) {
                data.setDiggingAction(1);
                data.setCurrentlyDigging(false);
            } else if (digAction == com.github.retrooper.packetevents.protocol.player.DiggingAction.FINISHED_DIGGING) {
                data.setDiggingAction(2);
                data.setCurrentlyDigging(false);
            } else {
                data.setDiggingAction(-1);
            }
            runChecks(cm.getPlayerChecksByCategory("fastbreak"), player, data);

        // === Window clicks (inventory) ===
        } else if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            runChecks(cm.getPlayerChecksByCategory("inventory"), player, data);

        // === Use item ===
        } else if (event.getPacketType() == PacketType.Play.Client.USE_ITEM) {
            data.setUsingItem(true);
            // Any right-click/use: guard aim checks for 300ms (players snap view to what they're using)
            data.setLastInteractionTime(System.currentTimeMillis());
            // Run checks BEFORE updating timestamp — FastUseA needs to compare against PREVIOUS use time
            runChecks(cm.getPlayerChecksByCategory("fastuse"), player, data);
            data.setLastItemUseTime(System.currentTimeMillis());
        }

        // Timer checks run on all packets — but use pre-indexed list (only 4 checks, not full iteration)
        runChecks(cm.getPlayerChecksByCategory("timer"), player, data);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        User user = event.getUser();
        if (user == null || user.getUUID() == null) return;

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(user.getUUID());
        if (data == null) return;

        Player player = data.getPlayer();
        if (player == null) return;

        // Track velocity sent by server
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_VELOCITY) {
            WrapperPlayServerEntityVelocity wrapper = new WrapperPlayServerEntityVelocity(event);
            if (wrapper.getEntityId() == player.getEntityId()) {
                data.setVelocity(
                        wrapper.getVelocity().getX(),
                        wrapper.getVelocity().getY(),
                        wrapper.getVelocity().getZ()
                );
            }
        }

        // Track teleports
        if (event.getPacketType() == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK) {
            data.setLastTeleportTime(System.currentTimeMillis());
            data.resetMovementsSinceLastTeleport();
        }
    }

    /**
     * Run a list of checks for a player. Skips disabled and unimplemented (stub) checks.
     * Uses a simple for loop — no allocation, no iterator object.
     */
    private void runChecks(List<Check> checks, Player player, PlayerData data) {
        for (int i = 0, size = checks.size(); i < size; i++) {
            Check check = checks.get(i);
            if (check.isEnabled() && check.isImplemented()) {
                check.handle(player, data);
            }
        }
    }
}
