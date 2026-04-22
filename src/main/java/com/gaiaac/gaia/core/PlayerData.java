package com.gaiaac.gaia.core;

import com.gaiaac.gaia.util.math.PredictionEngine;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerData {

    private final UUID uuid;
    private final String name;
    private transient Player player;

    // Cached potion effect amplifiers — updated from main thread, read from netty thread
    private volatile int speedAmplifier = -1; // -1 means no effect
    private volatile int jumpBoostAmplifier = -1;
    private volatile boolean hasLevitation = false;
    private volatile boolean hasSlowFalling = false;
    private volatile boolean wearingElytra = false; // true if elytra is equipped in chest slot
    private volatile boolean ridingJumpableVehicle = false; // true if riding a horse/camel (can jump)

    // Cached player attribute values — updated from main thread, read from netty thread
    // Using getAttribute().getValue() captures ALL modifiers: potions, commands, plugin modifications, etc.
    private volatile double movementSpeedAttribute = 0.1;   // Attribute.MOVEMENT_SPEED          default: 0.1
    private volatile double jumpStrengthAttribute  = 0.42;  // Attribute.JUMP_STRENGTH            default: 0.42
    private volatile double gravityAttribute       = 0.08;  // Attribute.GRAVITY                  default: 0.08
    private volatile double sneakingSpeedAttribute = 0.3;   // Attribute.SNEAKING_SPEED           default: 0.3 (Swift Sneak III → 0.75)
    private volatile double stepHeightAttribute    = 0.6;   // Attribute.STEP_HEIGHT              default: 0.6

    // Position tracking
    private double x, y, z;
    private double lastX, lastY, lastZ;
    private float yaw, pitch;
    private float lastYaw, lastPitch;
    private double deltaX, deltaY, deltaZ;
    private double deltaXZ;
    private float deltaYaw, deltaPitch;
    private float lastDeltaYaw, lastDeltaPitch;

    // Movement state
    private boolean onGround;
    private boolean lastOnGround;
    private boolean sprinting;
    private boolean sneaking;
    private boolean swimming;
    private boolean flying;
    private boolean gliding;
    private boolean inVehicle;
    private boolean inWater;
    private boolean inLava;
    private boolean onClimbable;
    private boolean onIce;
    private boolean onSlime;
    private volatile boolean inBubbleColumn;  // in BUBBLE_COLUMN block — propels player up/down
    private volatile boolean isRiptiding;     // using Riptide trident — launches player at high speed
    private volatile boolean onSoulBlock;     // on SOUL_SAND or SOUL_SOIL — Soul Speed enchant affects speed
    private volatile boolean onHoneyBlock;    // on HONEY_BLOCK — reduces movement speed + jump height

    // Combat tracking
    private long lastAttackTime;
    private long lastBlockPlaceTime;
    private long lastActualBlockPlaceTime; // set by BlockPlaceEvent — confirmed actual placement (not just right-click interaction)
    private float lastBlockPlaceYaw;   // yaw at last block placement (for scaffold rotation lock detection)
    private float lastBlockPlacePitch; // pitch at last block placement
    private int consecutiveAirPlacements; // how many consecutive placements while airborne
    private int clicksPerSecond;
    private final ArrayDeque<Long> clickTimestamps = new ArrayDeque<>(24);
    private UUID lastTargetUUID;
    private int lastTargetEntityId = -1;
    private int attackTargetCount; // entities attacked within current tick window
    private int lastAttackTargetEntityId = -1;
    private long lastAttackTargetSwitchTime;
    private boolean isUsingItem;
    private long lastItemUseTime;
    private long lastSneakToggleTime;    // time when sneaking started — grace for deceleration
    private long lastInteractionTime;    // time of last USE_ITEM or PLAYER_BLOCK_PLACEMENT — aim FP guard
    private volatile long lastExitWaterTime; // time when player left water — grace for leap/leap-out FPs
    private volatile long lastEnterWaterTime; // time when player entered water — used to gate exit grace (prevents Jesus skim exploit)
    private volatile long lastGlideStartTime; // time when player started gliding — grace for MotionB during elytra deployment
    private volatile boolean isSleeping;     // true while player is sleeping in a bed
    private float attackYaw;   // player's yaw at the moment they sent the last INTERACT_ENTITY attack packet
    private float attackPitch; // player's pitch at the moment they sent the last INTERACT_ENTITY attack packet

    // Digging / block breaking
    private volatile int diggingAction = -1; // 0=START, 1=CANCEL, 2=FINISH, -1=none
    private volatile boolean currentlyDigging = false; // true from START_DIGGING until FINISH/CANCEL
    private volatile org.bukkit.block.Block lastDigBlock;
    // Target block coords for freecam-dig check (set immediately on netty thread from packet)
    private volatile int lastDigTargetX;
    private volatile int lastDigTargetY;
    private volatile int lastDigTargetZ;

    // Timing
    private long joinTime;
    private long lastPacketTime;
    private long lastMovementPacket;
    private long lastTeleportTime;
    private int serverTick;
    private int movementsSinceLastTeleport;
    private final ArrayDeque<Long> packetTimestamps = new ArrayDeque<>(50);

    // Violations
    private final Map<String, Double> violations = new ConcurrentHashMap<>();
    private final Map<String, Long> lastViolationTime = new ConcurrentHashMap<>();
    private final Map<String, Double> buffers = new ConcurrentHashMap<>();

    // Client info
    private String clientBrand = "Unknown";
    private int clientProtocolVersion;
    private int ping;

    // Debug
    private boolean debugMode = false;
    private UUID debugTarget = null;

    // Inventory
    private boolean inventoryOpen = false;
    private long lastInventoryOpenTime;

    // Velocity
    private double velocityX, velocityY, velocityZ;
    private long lastVelocityTime;
    private boolean hasReceivedVelocity;

    // Misc
    private int airTicks;
    private int groundTicks;
    private int iceTicks;
    private double lastDeltaXZ;
    private double lastDeltaY;
    private double predictedY; // predicted deltaY for current tick (chasing — used by FlightA)

    // Physics prediction engine state (independent simulation — does NOT chase actual deltaY)
    // See PredictionEngine.java for the full description of how this differs from predictedY.
    private double simVY = 0.0;             // independent Y-velocity simulation
    private boolean simYValid = false;      // true when simVY is ready for comparison
    private double maxPredictedVXZ = Double.MAX_VALUE; // max allowed XZ speed for this tick

    // Cached permission — set once on join from main thread, read from netty thread
    private volatile boolean bypassed;
    private volatile boolean hasAlertsPermission;
    private volatile GameMode gameMode;

    public PlayerData(Player player) {
        this.player = player;
        this.uuid = player.getUniqueId();
        this.name = player.getName();
        this.joinTime = System.currentTimeMillis();
        Location loc = player.getLocation();
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.yaw = loc.getYaw();
        this.pitch = loc.getPitch();
        // Cache permissions on construction (always called from main thread)
        boolean explicitDeny = player.isPermissionSet("gaia.bypass") && !player.hasPermission("gaia.bypass");
        this.bypassed = !explicitDeny && (player.isOp() || player.hasPermission("gaia.bypass"));
        this.hasAlertsPermission = player.hasPermission("gaia.alerts");
        this.gameMode = player.getGameMode();
    }

    // === Position Updates ===

    public void handleMovement(double newX, double newY, double newZ, float newYaw, float newPitch, boolean onGround) {
        this.lastX = this.x;
        this.lastY = this.y;
        this.lastZ = this.z;
        this.lastYaw = this.yaw;
        this.lastPitch = this.pitch;
        this.lastOnGround = this.onGround;

        this.x = newX;
        this.y = newY;
        this.z = newZ;
        this.yaw = newYaw;
        this.pitch = newPitch;
        this.onGround = onGround;

        this.lastDeltaXZ = this.deltaXZ;
        this.lastDeltaY = this.deltaY;
        this.deltaX = newX - lastX;
        this.deltaY = newY - lastY;
        this.deltaZ = newZ - lastZ;
        this.deltaXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        this.lastDeltaYaw = this.deltaYaw;
        this.lastDeltaPitch = this.deltaPitch;
        // Wrap yaw difference to handle 360° wrapping (e.g., -179 to 179 = 2°, not 358°)
        float rawYawDiff = newYaw - lastYaw;
        while (rawYawDiff > 180f) rawYawDiff -= 360f;
        while (rawYawDiff < -180f) rawYawDiff += 360f;
        this.deltaYaw = Math.abs(rawYawDiff);
        this.deltaPitch = Math.abs(newPitch - lastPitch);

        this.lastMovementPacket = System.currentTimeMillis();
        this.movementsSinceLastTeleport++;

        if (onGround) {
            groundTicks++;
            airTicks = 0;
            predictedY = 0;
        } else {
            airTicks++;
            groundTicks = 0;
            // Chasing gravity prediction (used by FlightA):
            // velocity = (lastVelocity - gravity) * drag
            if (airTicks == 1 && lastOnGround) {
                predictedY = (deltaY - 0.08) * 0.98;
            } else {
                predictedY = (lastDeltaY - 0.08) * 0.98;
            }
        }

        // Advance the independent physics simulation (PredictionA/B)
        PredictionEngine.tick(this);
    }

    // === Violation Handling ===

    public double getVL(String check) {
        return violations.getOrDefault(check, 0.0);
    }

    public void addVL(String check, double amount) {
        violations.merge(check, amount, Double::sum);
        lastViolationTime.put(check, System.currentTimeMillis());
    }

    public void setVL(String check, double amount) {
        violations.put(check, amount);
        lastViolationTime.put(check, System.currentTimeMillis());
    }

    public void clearViolations() {
        violations.clear();
        lastViolationTime.clear();
    }

    public void decayViolations(double decayRate) {
        java.util.Iterator<Map.Entry<String, Double>> it = violations.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Double> entry = it.next();
            double newVal = entry.getValue() - decayRate;
            if (newVal <= 0) {
                it.remove();
            } else {
                entry.setValue(newVal);
            }
        }
    }

    public Map<String, Double> getViolations() {
        return Collections.unmodifiableMap(violations);
    }

    // === Buffer Handling ===

    public double getBuffer(String key) {
        return buffers.getOrDefault(key, 0.0);
    }

    public double addBuffer(String key, double amount) {
        double newVal = buffers.merge(key, amount, Double::sum);
        return newVal;
    }

    public void setBuffer(String key, double amount) {
        buffers.put(key, amount);
    }

    public double decreaseBuffer(String key, double amount) {
        double current = getBuffer(key);
        double newVal = Math.max(0, current - amount);
        buffers.put(key, newVal);
        return newVal;
    }

    // === Click Tracking (ArrayDeque — O(1) add/poll, no synchronization needed on netty thread) ===

    public void addClick() {
        long now = System.currentTimeMillis();
        clickTimestamps.addLast(now);
        // Trim old entries (older than 1 second) from front — O(1) per poll
        while (!clickTimestamps.isEmpty() && now - clickTimestamps.peekFirst() > 1000) {
            clickTimestamps.pollFirst();
        }
        clicksPerSecond = clickTimestamps.size();
    }

    public int getCPS() {
        long now = System.currentTimeMillis();
        while (!clickTimestamps.isEmpty() && now - clickTimestamps.peekFirst() > 1000) {
            clickTimestamps.pollFirst();
        }
        return clickTimestamps.size();
    }

    public List<Long> getClickTimestamps() {
        return new ArrayList<>(clickTimestamps);
    }

    // === Packet Timing (ArrayDeque — O(1) add/poll) ===

    public void addPacketTimestamp() {
        long now = System.currentTimeMillis();
        packetTimestamps.addLast(now);
        // Keep max 40 entries (~2 seconds at 20 TPS). Poll from front — O(1).
        while (packetTimestamps.size() > 40) {
            packetTimestamps.pollFirst();
        }
        lastPacketTime = now;
    }

    public long getTimeSinceLastPacket() {
        return System.currentTimeMillis() - lastPacketTime;
    }

    // === Getters / Setters ===

    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public double getLastX() { return lastX; }
    public double getLastY() { return lastY; }
    public double getLastZ() { return lastZ; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public float getLastYaw() { return lastYaw; }
    public float getLastPitch() { return lastPitch; }
    public double getDeltaX() { return deltaX; }
    public double getDeltaY() { return deltaY; }
    public double getDeltaZ() { return deltaZ; }
    public double getDeltaXZ() { return deltaXZ; }
    public float getDeltaYaw() { return deltaYaw; }
    public float getDeltaPitch() { return deltaPitch; }
    public float getLastDeltaYaw() { return lastDeltaYaw; }
    public float getLastDeltaPitch() { return lastDeltaPitch; }

    public boolean isOnGround() { return onGround; }
    public boolean wasOnGround() { return lastOnGround; }
    public boolean isSprinting() { return sprinting; }
    public void setSprinting(boolean sprinting) { this.sprinting = sprinting; }
    public boolean isSneaking() { return sneaking; }
    public void setSneaking(boolean sneaking) { this.sneaking = sneaking; }
    public boolean isSwimming() { return swimming; }
    public void setSwimming(boolean swimming) { this.swimming = swimming; }
    public boolean isFlying() { return flying; }
    public void setFlying(boolean flying) { this.flying = flying; }
    public boolean isGliding() { return gliding; }
    public void setGliding(boolean gliding) { this.gliding = gliding; }
    public boolean isWearingElytra() { return wearingElytra; }
    public void setWearingElytra(boolean wearingElytra) { this.wearingElytra = wearingElytra; }
    public boolean isRidingJumpableVehicle() { return ridingJumpableVehicle; }
    public void setRidingJumpableVehicle(boolean v) { this.ridingJumpableVehicle = v; }
    public boolean isInVehicle() { return inVehicle; }
    public void setInVehicle(boolean inVehicle) { this.inVehicle = inVehicle; }
    public boolean isInWater() { return inWater; }
    public void setInWater(boolean inWater) { this.inWater = inWater; }
    public boolean isInLava() { return inLava; }
    public void setInLava(boolean inLava) { this.inLava = inLava; }
    public boolean isOnClimbable() { return onClimbable; }
    public void setOnClimbable(boolean onClimbable) { this.onClimbable = onClimbable; }
    public boolean isOnIce() { return onIce; }
    public void setOnIce(boolean onIce) { this.onIce = onIce; }
    public boolean isOnSlime() { return onSlime; }
    public void setOnSlime(boolean onSlime) { this.onSlime = onSlime; }
    public boolean isInBubbleColumn() { return inBubbleColumn; }
    public void setInBubbleColumn(boolean v) { this.inBubbleColumn = v; }
    public boolean isRiptiding() { return isRiptiding; }
    public void setRiptiding(boolean v) { this.isRiptiding = v; }
    public boolean isOnSoulBlock() { return onSoulBlock; }
    public void setOnSoulBlock(boolean v) { this.onSoulBlock = v; }
    public boolean isOnHoneyBlock() { return onHoneyBlock; }
    public void setOnHoneyBlock(boolean v) { this.onHoneyBlock = v; }

    public long getLastAttackTime() { return lastAttackTime; }
    public void setLastAttackTime(long lastAttackTime) { this.lastAttackTime = lastAttackTime; }
    public long getLastBlockPlaceTime() { return lastBlockPlaceTime; }
    public void setLastBlockPlaceTime(long lastBlockPlaceTime) { this.lastBlockPlaceTime = lastBlockPlaceTime; }
    public long getLastActualBlockPlaceTime() { return lastActualBlockPlaceTime; }
    public void setLastActualBlockPlaceTime(long t) { this.lastActualBlockPlaceTime = t; }
    public float getLastBlockPlaceYaw() { return lastBlockPlaceYaw; }
    public void setLastBlockPlaceYaw(float yaw) { this.lastBlockPlaceYaw = yaw; }
    public float getLastBlockPlacePitch() { return lastBlockPlacePitch; }
    public void setLastBlockPlacePitch(float pitch) { this.lastBlockPlacePitch = pitch; }
    public int getConsecutiveAirPlacements() { return consecutiveAirPlacements; }
    public void setConsecutiveAirPlacements(int count) { this.consecutiveAirPlacements = count; }
    public int getDiggingAction() { return diggingAction; }
    public void setDiggingAction(int action) { this.diggingAction = action; }
    public boolean isCurrentlyDigging() { return currentlyDigging; }
    public void setCurrentlyDigging(boolean v) { this.currentlyDigging = v; }
    public org.bukkit.block.Block getLastDigBlock() { return lastDigBlock; }
    public void setLastDigBlock(org.bukkit.block.Block block) { this.lastDigBlock = block; }
    public int getLastDigTargetX() { return lastDigTargetX; }
    public int getLastDigTargetY() { return lastDigTargetY; }
    public int getLastDigTargetZ() { return lastDigTargetZ; }
    public void setLastDigTarget(int x, int y, int z) { this.lastDigTargetX = x; this.lastDigTargetY = y; this.lastDigTargetZ = z; }
    public int getClicksPerSecond() { return clicksPerSecond; }
    public UUID getLastTargetUUID() { return lastTargetUUID; }
    public void setLastTargetUUID(UUID lastTargetUUID) { this.lastTargetUUID = lastTargetUUID; }

    public long getJoinTime() { return joinTime; }
    public void setJoinTime(long joinTime) { this.joinTime = joinTime; }
    public long getLastPacketTime() { return lastPacketTime; }
    public long getLastMovementPacket() { return lastMovementPacket; }
    public long getLastTeleportTime() { return lastTeleportTime; }
    public void setLastTeleportTime(long lastTeleportTime) { this.lastTeleportTime = lastTeleportTime; }
    public int getServerTick() { return serverTick; }
    public void setServerTick(int serverTick) { this.serverTick = serverTick; }
    public int getMovementsSinceLastTeleport() { return movementsSinceLastTeleport; }
    public void resetMovementsSinceLastTeleport() { this.movementsSinceLastTeleport = 0; }
    public List<Long> getPacketTimestamps() { return new ArrayList<>(packetTimestamps); }

    public String getClientBrand() { return clientBrand; }
    public void setClientBrand(String clientBrand) { this.clientBrand = clientBrand; }
    public int getClientProtocolVersion() { return clientProtocolVersion; }
    public void setClientProtocolVersion(int clientProtocolVersion) { this.clientProtocolVersion = clientProtocolVersion; }
    public int getPing() { return ping; }
    public void setPing(int ping) { this.ping = ping; }

    public boolean isDebugMode() { return debugMode; }
    public void setDebugMode(boolean debugMode) { this.debugMode = debugMode; }
    public UUID getDebugTarget() { return debugTarget; }
    public void setDebugTarget(UUID debugTarget) { this.debugTarget = debugTarget; }

    public boolean isInventoryOpen() { return inventoryOpen; }
    public void setInventoryOpen(boolean inventoryOpen) { this.inventoryOpen = inventoryOpen; }
    public long getLastInventoryOpenTime() { return lastInventoryOpenTime; }
    public void setLastInventoryOpenTime(long lastInventoryOpenTime) { this.lastInventoryOpenTime = lastInventoryOpenTime; }

    public double getVelocityX() { return velocityX; }
    public double getVelocityY() { return velocityY; }
    public double getVelocityZ() { return velocityZ; }
    public void setVelocity(double vx, double vy, double vz) {
        this.velocityX = vx;
        this.velocityY = vy;
        this.velocityZ = vz;
        this.lastVelocityTime = System.currentTimeMillis();
        this.hasReceivedVelocity = true;
    }
    public long getLastVelocityTime() { return lastVelocityTime; }
    public boolean hasReceivedVelocity() { return hasReceivedVelocity; }
    public void setHasReceivedVelocity(boolean hasReceivedVelocity) { this.hasReceivedVelocity = hasReceivedVelocity; }

    public int getAirTicks() { return airTicks; }
    public void setAirTicks(int airTicks) { this.airTicks = airTicks; }
    public int getGroundTicks() { return groundTicks; }
    public void setGroundTicks(int groundTicks) { this.groundTicks = groundTicks; }
    public int getIceTicks() { return iceTicks; }
    public void setIceTicks(int iceTicks) { this.iceTicks = iceTicks; }
    public double getLastDeltaXZ() { return lastDeltaXZ; }
    public double getLastDeltaY() { return lastDeltaY; }
    public double getPredictedY() { return predictedY; }

    // === Prediction engine state (independent Y simulation + XZ max speed) ===
    public double getSimVY() { return simVY; }
    public void setSimVY(double v) { this.simVY = v; }
    public boolean isSimYValid() { return simYValid; }
    public void setSimYValid(boolean v) { this.simYValid = v; }
    public double getMaxPredictedVXZ() { return maxPredictedVXZ; }
    public void setMaxPredictedVXZ(double v) { this.maxPredictedVXZ = v; }

    public int getLastTargetEntityId() { return lastTargetEntityId; }
    public void setLastTargetEntityId(int id) { this.lastTargetEntityId = id; }
    public int getLastAttackTargetEntityId() { return lastAttackTargetEntityId; }
    public void setLastAttackTargetEntityId(int id) { this.lastAttackTargetEntityId = id; }
    public int getAttackTargetCount() { return attackTargetCount; }
    public void setAttackTargetCount(int count) { this.attackTargetCount = count; }
    public long getLastAttackTargetSwitchTime() { return lastAttackTargetSwitchTime; }
    public void setLastAttackTargetSwitchTime(long time) { this.lastAttackTargetSwitchTime = time; }
    public boolean isUsingItem() { return isUsingItem; }
    public void setUsingItem(boolean usingItem) { this.isUsingItem = usingItem; }
    public long getLastItemUseTime() { return lastItemUseTime; }
    public void setLastItemUseTime(long time) { this.lastItemUseTime = time; }
    public long getLastSneakToggleTime() { return lastSneakToggleTime; }
    public void setLastSneakToggleTime(long time) { this.lastSneakToggleTime = time; }
    public long getLastInteractionTime() { return lastInteractionTime; }
    public void setLastInteractionTime(long time) { this.lastInteractionTime = time; }
    public long getLastExitWaterTime() { return lastExitWaterTime; }
    public void setLastExitWaterTime(long time) { this.lastExitWaterTime = time; }
    public long getLastEnterWaterTime() { return lastEnterWaterTime; }
    public void setLastEnterWaterTime(long time) { this.lastEnterWaterTime = time; }
    public long getLastGlideStartTime() { return lastGlideStartTime; }
    public void setLastGlideStartTime(long time) { this.lastGlideStartTime = time; }
    public boolean isSleeping() { return isSleeping; }
    public void setSleeping(boolean sleeping) { this.isSleeping = sleeping; }
    public float getAttackYaw() { return attackYaw; }
    public void setAttackYaw(float yaw) { this.attackYaw = yaw; }
    public float getAttackPitch() { return attackPitch; }
    public void setAttackPitch(float pitch) { this.attackPitch = pitch; }

    public boolean isBypassed() { return bypassed; }
    public void setBypassed(boolean bypassed) { this.bypassed = bypassed; }
    public boolean hasAlertsPermission() { return hasAlertsPermission; }
    public void setAlertsPermission(boolean hasAlertsPermission) { this.hasAlertsPermission = hasAlertsPermission; }

    public GameMode getGameMode() { return gameMode; }
    public void setGameMode(GameMode gameMode) { this.gameMode = gameMode; }

    public boolean isExempt() {
        return bypassed || gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR;
    }

    // === Cached Potion Effects (set from main thread, read from netty thread) ===
    public int getSpeedAmplifier() { return speedAmplifier; }
    public void setSpeedAmplifier(int amp) { this.speedAmplifier = amp; }
    public int getJumpBoostAmplifier() { return jumpBoostAmplifier; }
    public void setJumpBoostAmplifier(int amp) { this.jumpBoostAmplifier = amp; }
    public boolean hasLevitation() { return hasLevitation; }
    public void setHasLevitation(boolean has) { this.hasLevitation = has; }
    public boolean hasSlowFalling() { return hasSlowFalling; }
    public void setHasSlowFalling(boolean v) { this.hasSlowFalling = v; }

    // === Cached Attribute Values (set from main thread, read from netty thread) ===
    // getValue() returns the final computed value including ALL modifiers (potions, /attribute, plugins, etc.)
    public double getMovementSpeedAttribute() { return movementSpeedAttribute; }
    public void setMovementSpeedAttribute(double v) { this.movementSpeedAttribute = v; }
    public double getJumpStrengthAttribute() { return jumpStrengthAttribute; }
    public void setJumpStrengthAttribute(double v) { this.jumpStrengthAttribute = v; }
    public double getGravityAttribute() { return gravityAttribute; }
    public void setGravityAttribute(double v) { this.gravityAttribute = v; }
    public double getSneakingSpeedAttribute() { return sneakingSpeedAttribute; }
    public void setSneakingSpeedAttribute(double v) { this.sneakingSpeedAttribute = v; }
    public double getStepHeightAttribute() { return stepHeightAttribute; }
    public void setStepHeightAttribute(double v) { this.stepHeightAttribute = v; }
}
