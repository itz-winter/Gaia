package com.gaiaac.gaia.core;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerData {

    private final UUID uuid;
    private final String name;
    private transient Player player;

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

    // Combat tracking
    private long lastAttackTime;
    private long lastBlockPlaceTime;
    private int clicksPerSecond;
    private final List<Long> clickTimestamps = Collections.synchronizedList(new ArrayList<>());
    private UUID lastTargetUUID;
    private int lastTargetEntityId = -1;
    private int attackTargetCount; // entities attacked within current tick window
    private int lastAttackTargetEntityId = -1;
    private long lastAttackTargetSwitchTime;
    private boolean isUsingItem;
    private long lastItemUseTime;

    // Timing
    private long joinTime;
    private long lastPacketTime;
    private long lastMovementPacket;
    private long lastTeleportTime;
    private int serverTick;
    private int movementsSinceLastTeleport;
    private final List<Long> packetTimestamps = Collections.synchronizedList(new ArrayList<>(50));

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
    private double predictedY; // predicted Y from gravity simulation

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
        this.bypassed = player.isOp() || player.hasPermission("gaia.bypass");
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
        this.deltaYaw = Math.abs(newYaw - lastYaw);
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
            // Gravity simulation: velocity = (lastVelocity - 0.08) * 0.98
            if (airTicks == 1 && lastOnGround) {
                // First airborne tick after jump
                predictedY = (deltaY - 0.08) * 0.98;
            } else {
                predictedY = (lastDeltaY - 0.08) * 0.98;
            }
        }
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

    // === Click Tracking ===

    public void addClick() {
        long now = System.currentTimeMillis();
        clickTimestamps.add(now);
        // Trim old entries (older than 1 second) from front
        while (!clickTimestamps.isEmpty() && now - clickTimestamps.get(0) > 1000) {
            clickTimestamps.remove(0);
        }
        clicksPerSecond = clickTimestamps.size();
    }

    public int getCPS() {
        long now = System.currentTimeMillis();
        while (!clickTimestamps.isEmpty() && now - clickTimestamps.get(0) > 1000) {
            clickTimestamps.remove(0);
        }
        return clickTimestamps.size();
    }

    public List<Long> getClickTimestamps() {
        return clickTimestamps;
    }

    // === Packet Timing ===

    public void addPacketTimestamp() {
        long now = System.currentTimeMillis();
        packetTimestamps.add(now);
        // Keep max 40 entries (~2 seconds at 20 TPS). Trim from front — O(1) amortized.
        while (packetTimestamps.size() > 40) {
            packetTimestamps.remove(0);
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

    public long getLastAttackTime() { return lastAttackTime; }
    public void setLastAttackTime(long lastAttackTime) { this.lastAttackTime = lastAttackTime; }
    public long getLastBlockPlaceTime() { return lastBlockPlaceTime; }
    public void setLastBlockPlaceTime(long lastBlockPlaceTime) { this.lastBlockPlaceTime = lastBlockPlaceTime; }
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
    public List<Long> getPacketTimestamps() { return packetTimestamps; }

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

    public boolean isBypassed() { return bypassed; }
    public void setBypassed(boolean bypassed) { this.bypassed = bypassed; }
    public boolean hasAlertsPermission() { return hasAlertsPermission; }
    public void setAlertsPermission(boolean hasAlertsPermission) { this.hasAlertsPermission = hasAlertsPermission; }

    public GameMode getGameMode() { return gameMode; }
    public void setGameMode(GameMode gameMode) { this.gameMode = gameMode; }

    public boolean isExempt() {
        return bypassed || gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR;
    }
}
