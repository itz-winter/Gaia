package com.gaiaac.gaia.util.math;

import com.gaiaac.gaia.core.PlayerData;

/**
 * Vanilla movement prediction engine.
 *
 * Called once per movement packet at the end of PlayerData.handleMovement().
 * Maintains two independently-tracked values:
 *
 *  simVY          — an independent Y-velocity simulation that does NOT chase the
 *                   player's actual deltaY. It is initialised from a known-good state
 *                   (landing resets to 0, first airborne tick accepts actual jump dY)
 *                   and then evolves purely under physics. A cheating player's actual
 *                   deltaY will diverge from simVY by an increasing amount each tick.
 *                   This is the fundamental difference from the existing predictedY,
 *                   which resets from actual lastDeltaY every tick (chasing the cheat).
 *
 *  maxPredictedVXZ — physics-upper-bound for horizontal speed this tick, derived from
 *                    movement speed attribute, sprint/sneak state, medium, and ping.
 *
 * Environment constants match Minecraft 1.21 vanilla:
 *   Air:   gravity = 0.08,  drag = 0.98
 *   Water: gravity = 0.02,  drag = 0.80
 *   Lava:  gravity = 0.02,  drag = 0.50
 *   Slow Falling: gravity = 0.01, drag = 0.98
 */
public final class PredictionEngine {

    private PredictionEngine() {}

    // ── Y physics ────────────────────────────────────────────────────────────
    private static final double GRAVITY_AIR  = 0.08;
    private static final double GRAVITY_SLOW = 0.01;
    private static final double GRAVITY_WATER = 0.02;
    private static final double GRAVITY_LAVA  = 0.02;
    private static final double DRAG_AIR   = 0.98;
    private static final double DRAG_WATER = 0.80;
    private static final double DRAG_LAVA  = 0.50;

    // ── XZ physics ───────────────────────────────────────────────────────────
    // Empirical vanilla maximums at default movement speed attribute (0.1):
    //   Sprint on flat  ≈ 0.292 b/t, ceiling at ~0.36 with momentum
    //   Walk            ≈ 0.215 b/t
    //   Sneak (default) ≈ 0.065 b/t
    //   Sprint-swim     ≈ 0.22  b/t
    private static final double MAX_SPRINT_BASE = 0.36;
    private static final double MAX_WALK_BASE   = 0.30;
    private static final double MAX_SWIM_BASE   = 0.22;
    private static final double DEFAULT_MOVE_SPEED   = 0.1;
    private static final double DEFAULT_SNEAK_SPEED  = 0.3;

    /**
     * Tick the engine: updates simVY and maxPredictedVXZ stored in PlayerData.
     * Must be called AFTER handleMovement() has updated deltaY / lastDeltaY.
     */
    public static void tick(PlayerData data) {
        tickY(data);
        tickXZ(data);
    }

    // ── Y simulation ─────────────────────────────────────────────────────────

    private static void tickY(PlayerData data) {
        // Cannot make meaningful predictions in any of these states
        if (data.isFlying() || data.isGliding() || data.isInVehicle()
                || data.isOnClimbable() || data.isRiptiding()
                || data.isInBubbleColumn() || data.hasLevitation()) {
            data.setSimYValid(false);
            return;
        }

        // A server velocity packet was received recently — player's Y is externally controlled;
        // do not simulate until they land and reset.
        // recentlyReceivedVelocity() check is done by checks themselves; here we just check the flag.
        if (data.hasReceivedVelocity() &&
                System.currentTimeMillis() - data.getLastVelocityTime() < 1500) {
            data.setSimYValid(false);
            return;
        }

        boolean nowOnGround  = data.isOnGround();
        boolean wasOnGround  = data.wasOnGround();

        // Just landed (air → ground) or on ground both ticks: reset sim
        if (nowOnGround) {
            data.setSimVY(0.0);
            data.setSimYValid(false); // don't compare on landing tick itself
            return;
        }

        int airTicks = data.getAirTicks();

        // First tick airborne (ground → air): accept the actual jump velocity as the
        // simulation's starting point. We can't know when a jump will happen, so we
        // initialise from the real value to avoid false-positive on jump start.
        if (airTicks == 1 && wasOnGround) {
            data.setSimVY(data.getDeltaY()); // accept actual jump dY
            data.setSimYValid(false);        // first tick — wait one more before comparing
            return;
        }

        // airTicks == 1 but wasOnGround is false can happen at edges/portals — skip
        if (airTicks < 2) {
            data.setSimYValid(false);
            return;
        }

        // airTicks >= 2, fully airborne both ticks: advance the independent simulation
        double simVY = data.getSimVY();
        double predictedVY;
        if (data.isInWater()) {
            predictedVY = (simVY - GRAVITY_WATER) * DRAG_WATER;
        } else if (data.isInLava()) {
            predictedVY = (simVY - GRAVITY_LAVA) * DRAG_LAVA;
        } else {
            double gravity = data.hasSlowFalling() ? GRAVITY_SLOW : data.getGravityAttribute();
            predictedVY = (simVY - gravity) * DRAG_AIR;
        }

        data.setSimVY(predictedVY); // advance sim regardless — keeps accumulating divergence
        data.setSimYValid(true);    // ready for comparison this tick
    }

    // ── XZ max speed ─────────────────────────────────────────────────────────

    private static void tickXZ(PlayerData data) {
        // States where horizontal bounds can't be reliably computed
        if (data.isFlying() || data.isGliding() || data.isRiptiding()
                || data.isInBubbleColumn() || data.isInVehicle()
                || data.isOnIce() || data.isOnSlime()) {
            data.setMaxPredictedVXZ(Double.MAX_VALUE);
            return;
        }

        // Speed scale: captures ALL modifiers (potions, /attribute, plugins) via getValue()
        double speedScale = data.getMovementSpeedAttribute() / DEFAULT_MOVE_SPEED;

        double max;
        if (data.isInWater() || data.isInLava()) {
            max = MAX_SWIM_BASE * speedScale;
        } else if (data.isSneaking() && data.isOnGround()) {
            // Swift Sneak III brings SNEAKING_SPEED from 0.3 → ~0.75
            double sneakScale = data.getSneakingSpeedAttribute() / DEFAULT_SNEAK_SPEED;
            max = 0.11 * speedScale * sneakScale;
        } else {
            // Walking or sprinting — use sprint ceiling as the upper bound.
            // Can't distinguish from packets alone (sprint flag is from Bukkit event which may lag).
            max = MAX_SPRINT_BASE * speedScale;
        }

        // Honey block reduces movement
        if (data.isOnHoneyBlock()) max *= 0.6;

        // Ping compensation: each 250ms of latency can manifest as ~0.05 extra b/t observed
        double pingTol = Math.min(data.getPing() / 1000.0, 0.25);
        max += pingTol;

        // Hard floor — prevents division-by-zero and overly tight bounds at very low speeds
        max = Math.max(max, 0.40);

        data.setMaxPredictedVXZ(max);
    }
}
