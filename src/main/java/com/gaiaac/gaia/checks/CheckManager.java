package com.gaiaac.gaia.checks;

import com.gaiaac.gaia.checks.combat.*;
import com.gaiaac.gaia.checks.movement.*;
import com.gaiaac.gaia.checks.player.*;
import com.gaiaac.gaia.core.GaiaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckManager {

    private final GaiaPlugin plugin;
    private final List<Check> combatChecks = new ArrayList<>();
    private final List<Check> movementChecks = new ArrayList<>();
    private final List<Check> playerChecks = new ArrayList<>();

    // Pre-indexed category lists for O(1) dispatch — avoids iterating + string compare per packet
    private final Map<String, List<Check>> combatByCategory = new HashMap<>();
    private final Map<String, List<Check>> movementByCategory = new HashMap<>();
    private final Map<String, List<Check>> playerByCategory = new HashMap<>();

    public CheckManager(GaiaPlugin plugin) {
        this.plugin = plugin;
        registerChecks();
    }

    private void registerChecks() {
        // === COMBAT CHECKS ===
        registerCombatChecks();

        // === MOVEMENT CHECKS ===
        registerMovementChecks();

        // === PLAYER CHECKS ===
        registerPlayerChecks();

        // Build category indexes for fast O(1) dispatch
        buildCategoryIndex(combatChecks, combatByCategory);
        buildCategoryIndex(movementChecks, movementByCategory);
        buildCategoryIndex(playerChecks, playerByCategory);

        plugin.getLogger().info("Registered " + (combatChecks.size() + movementChecks.size() + playerChecks.size()) + " checks.");
    }

    private void buildCategoryIndex(List<Check> checks, Map<String, List<Check>> index) {
        for (Check check : checks) {
            index.computeIfAbsent(check.getCheckCategory(), k -> new ArrayList<>()).add(check);
        }
    }

    private void registerCombatChecks() {
        // Aim checks (A-Y)
        combatChecks.add(new AimA(plugin));
        combatChecks.add(new AimB(plugin));
        combatChecks.add(new AimC(plugin));
        combatChecks.add(new AimD(plugin));
        combatChecks.add(new AimE(plugin));
        combatChecks.add(new AimF(plugin));
        combatChecks.add(new AimG(plugin));
        combatChecks.add(new AimH(plugin));
        combatChecks.add(new AimI(plugin));
        combatChecks.add(new AimJ(plugin));
        combatChecks.add(new AimK(plugin));
        combatChecks.add(new AimL(plugin));
        combatChecks.add(new AimM(plugin));
        combatChecks.add(new AimN(plugin));
        combatChecks.add(new AimO(plugin));
        combatChecks.add(new AimP(plugin));
        combatChecks.add(new AimQ(plugin));
        combatChecks.add(new AimR(plugin));
        combatChecks.add(new AimS(plugin));
        combatChecks.add(new AimT(plugin));
        combatChecks.add(new AimU(plugin));
        combatChecks.add(new AimV(plugin));
        combatChecks.add(new AimW(plugin));
        combatChecks.add(new AimX(plugin));
        combatChecks.add(new AimY(plugin));

        // AutoBlock checks (A-D)
        combatChecks.add(new AutoBlockA(plugin));
        combatChecks.add(new AutoBlockB(plugin));
        combatChecks.add(new AutoBlockC(plugin));
        combatChecks.add(new AutoBlockD(plugin));

        // AutoClicker checks (A-T)
        combatChecks.add(new AutoClickerA(plugin));
        combatChecks.add(new AutoClickerB(plugin));
        combatChecks.add(new AutoClickerC(plugin));
        combatChecks.add(new AutoClickerD(plugin));
        combatChecks.add(new AutoClickerE(plugin));
        combatChecks.add(new AutoClickerF(plugin));
        combatChecks.add(new AutoClickerG(plugin));
        combatChecks.add(new AutoClickerH(plugin));
        combatChecks.add(new AutoClickerI(plugin));
        combatChecks.add(new AutoClickerJ(plugin));
        combatChecks.add(new AutoClickerK(plugin));
        combatChecks.add(new AutoClickerL(plugin));
        combatChecks.add(new AutoClickerM(plugin));
        combatChecks.add(new AutoClickerN(plugin));
        combatChecks.add(new AutoClickerO(plugin));
        combatChecks.add(new AutoClickerP(plugin));
        combatChecks.add(new AutoClickerQ(plugin));
        combatChecks.add(new AutoClickerR(plugin));
        combatChecks.add(new AutoClickerS(plugin));
        combatChecks.add(new AutoClickerT(plugin));

        // Criticals (A-B)
        combatChecks.add(new CriticalsA(plugin));
        combatChecks.add(new CriticalsB(plugin));

        // FastBow (A)
        combatChecks.add(new FastBowA(plugin));

        // Hitbox (A-B)
        combatChecks.add(new HitboxA(plugin));
        combatChecks.add(new HitboxB(plugin));

        // KillAura (A-L)
        combatChecks.add(new KillAuraA(plugin));
        combatChecks.add(new KillAuraB(plugin));
        combatChecks.add(new KillAuraC(plugin));
        combatChecks.add(new KillAuraD(plugin));
        combatChecks.add(new KillAuraE(plugin));
        combatChecks.add(new KillAuraF(plugin));
        combatChecks.add(new KillAuraG(plugin));
        combatChecks.add(new KillAuraH(plugin));
        combatChecks.add(new KillAuraI(plugin));
        combatChecks.add(new KillAuraJ(plugin));
        combatChecks.add(new KillAuraK(plugin));
        combatChecks.add(new KillAuraL(plugin));

        // Reach (A-B)
        combatChecks.add(new ReachA(plugin));
        combatChecks.add(new ReachB(plugin));

        // Velocity (A-D)
        combatChecks.add(new VelocityA(plugin));
        combatChecks.add(new VelocityB(plugin));
        combatChecks.add(new VelocityC(plugin));
        combatChecks.add(new VelocityD(plugin));
    }

    private void registerMovementChecks() {
        // AntiLevitation (A)
        movementChecks.add(new AntiLevitationA(plugin));

        // BoatFly (A-C)
        movementChecks.add(new BoatFlyA(plugin));
        movementChecks.add(new BoatFlyB(plugin));
        movementChecks.add(new BoatFlyC(plugin));

        // EntitySpeed (A)
        movementChecks.add(new EntitySpeedA(plugin));

        // EntityFlight (A-B)
        movementChecks.add(new EntityFlightA(plugin));
        movementChecks.add(new EntityFlightB(plugin));

        // Elytra (A-M)
        movementChecks.add(new ElytraA(plugin));
        movementChecks.add(new ElytraB(plugin));
        movementChecks.add(new ElytraC(plugin));
        movementChecks.add(new ElytraD(plugin));
        movementChecks.add(new ElytraE(plugin));
        movementChecks.add(new ElytraF(plugin));
        movementChecks.add(new ElytraG(plugin));
        movementChecks.add(new ElytraH(plugin));
        movementChecks.add(new ElytraI(plugin));
        movementChecks.add(new ElytraJ(plugin));
        movementChecks.add(new ElytraK(plugin));
        movementChecks.add(new ElytraL(plugin));
        movementChecks.add(new ElytraM(plugin));

        // FastClimb (A)
        movementChecks.add(new FastClimbA(plugin));

        // Flight (A-E)
        movementChecks.add(new FlightA(plugin));
        movementChecks.add(new FlightB(plugin));
        movementChecks.add(new FlightC(plugin));
        movementChecks.add(new FlightD(plugin));
        movementChecks.add(new FlightE(plugin));

        // Jesus (A-E)
        movementChecks.add(new JesusA(plugin));
        movementChecks.add(new JesusB(plugin));
        movementChecks.add(new JesusC(plugin));
        movementChecks.add(new JesusD(plugin));
        movementChecks.add(new JesusE(plugin));

        // Jump (A-B)
        movementChecks.add(new JumpA(plugin));
        movementChecks.add(new JumpB(plugin));

        // Motion (A-H)
        movementChecks.add(new MotionA(plugin));
        movementChecks.add(new MotionB(plugin));
        movementChecks.add(new MotionC(plugin));
        movementChecks.add(new MotionD(plugin));
        movementChecks.add(new MotionE(plugin));
        movementChecks.add(new MotionF(plugin));
        movementChecks.add(new MotionG(plugin));
        movementChecks.add(new MotionH(plugin));

        // NoSaddle (A)
        movementChecks.add(new NoSaddleA(plugin));

        // NoSlow (A-C)
        movementChecks.add(new NoSlowA(plugin));
        movementChecks.add(new NoSlowB(plugin));
        movementChecks.add(new NoSlowC(plugin));

        // Speed (A-D)
        movementChecks.add(new SpeedA(plugin));
        movementChecks.add(new SpeedB(plugin));
        movementChecks.add(new SpeedC(plugin));
        movementChecks.add(new SpeedD(plugin));

        // Step (A-C)
        movementChecks.add(new StepA(plugin));
        movementChecks.add(new StepB(plugin));
        movementChecks.add(new StepC(plugin));

        // Sprint (A)
        movementChecks.add(new SprintA(plugin));

        // Strafe (A)
        movementChecks.add(new StrafeA(plugin));

        // Scaffold (A-O)
        movementChecks.add(new ScaffoldA(plugin));
        movementChecks.add(new ScaffoldB(plugin));
        movementChecks.add(new ScaffoldC(plugin));
        movementChecks.add(new ScaffoldD(plugin));
        movementChecks.add(new ScaffoldE(plugin));
        movementChecks.add(new ScaffoldF(plugin));
        movementChecks.add(new ScaffoldG(plugin));
        movementChecks.add(new ScaffoldH(plugin));
        movementChecks.add(new ScaffoldI(plugin));
        movementChecks.add(new ScaffoldJ(plugin));
        movementChecks.add(new ScaffoldK(plugin));
        movementChecks.add(new ScaffoldL(plugin));
        movementChecks.add(new ScaffoldM(plugin));
        movementChecks.add(new ScaffoldN(plugin));
        movementChecks.add(new ScaffoldO(plugin));

        // VClip (A)
        movementChecks.add(new VClipA(plugin));

        // WallClimb (A)
        movementChecks.add(new WallClimbA(plugin));
    }

    private void registerPlayerChecks() {
        // AirPlace (A)
        playerChecks.add(new AirPlaceA(plugin));

        // BadPackets (A-Z, 1-8)
        playerChecks.add(new BadPacketsA(plugin));
        playerChecks.add(new BadPacketsB(plugin));
        playerChecks.add(new BadPacketsC(plugin));
        playerChecks.add(new BadPacketsD(plugin));
        playerChecks.add(new BadPacketsE(plugin));
        playerChecks.add(new BadPacketsF(plugin));
        playerChecks.add(new BadPacketsG(plugin));
        playerChecks.add(new BadPacketsH(plugin));
        playerChecks.add(new BadPacketsI(plugin));
        playerChecks.add(new BadPacketsJ(plugin));
        playerChecks.add(new BadPacketsK(plugin));
        playerChecks.add(new BadPacketsL(plugin));
        playerChecks.add(new BadPacketsM(plugin));
        playerChecks.add(new BadPacketsN(plugin));
        playerChecks.add(new BadPacketsO(plugin));
        playerChecks.add(new BadPacketsP(plugin));
        playerChecks.add(new BadPacketsQ(plugin));
        playerChecks.add(new BadPacketsR(plugin));
        playerChecks.add(new BadPacketsS(plugin));
        playerChecks.add(new BadPacketsT(plugin));
        playerChecks.add(new BadPacketsU(plugin));
        playerChecks.add(new BadPacketsV(plugin));
        playerChecks.add(new BadPacketsW(plugin));
        playerChecks.add(new BadPacketsX(plugin));
        playerChecks.add(new BadPacketsY(plugin));
        playerChecks.add(new BadPacketsZ(plugin));
        playerChecks.add(new BadPackets1(plugin));
        playerChecks.add(new BadPackets2(plugin));
        playerChecks.add(new BadPackets3(plugin));
        playerChecks.add(new BadPackets4(plugin));
        playerChecks.add(new BadPackets5(plugin));
        playerChecks.add(new BadPackets6(plugin));
        playerChecks.add(new BadPackets7(plugin));
        playerChecks.add(new BadPackets8(plugin));

        // Baritone (A-B)
        playerChecks.add(new BaritoneA(plugin));
        playerChecks.add(new BaritoneB(plugin));

        // FastBreak (A)
        playerChecks.add(new FastBreakA(plugin));

        // FastPlace (A)
        playerChecks.add(new FastPlaceA(plugin));

        // FastUse (A)
        playerChecks.add(new FastUseA(plugin));

        // GroundSpoof (A-C)
        playerChecks.add(new GroundSpoofA(plugin));
        playerChecks.add(new GroundSpoofB(plugin));
        playerChecks.add(new GroundSpoofC(plugin));

        // GhostHand (A)
        playerChecks.add(new GhostHandA(plugin));

        // Improbable (A-F)
        playerChecks.add(new ImprobableA(plugin));
        playerChecks.add(new ImprobableB(plugin));
        playerChecks.add(new ImprobableC(plugin));
        playerChecks.add(new ImprobableD(plugin));
        playerChecks.add(new ImprobableE(plugin));
        playerChecks.add(new ImprobableF(plugin));

        // Invalid (A-J)
        playerChecks.add(new InvalidA(plugin));
        playerChecks.add(new InvalidB(plugin));
        playerChecks.add(new InvalidC(plugin));
        playerChecks.add(new InvalidD(plugin));
        playerChecks.add(new InvalidE(plugin));
        playerChecks.add(new InvalidF(plugin));
        playerChecks.add(new InvalidG(plugin));
        playerChecks.add(new InvalidH(plugin));
        playerChecks.add(new InvalidI(plugin));
        playerChecks.add(new InvalidJ(plugin));

        // Inventory (A-B)
        playerChecks.add(new InventoryA(plugin));
        playerChecks.add(new InventoryB(plugin));

        // Timer (A-D)
        playerChecks.add(new TimerA(plugin));
        playerChecks.add(new TimerB(plugin));
        playerChecks.add(new TimerC(plugin));
        playerChecks.add(new TimerD(plugin));

        // Tower (A)
        playerChecks.add(new TowerA(plugin));
    }

    public List<Check> getCombatChecks() {
        return Collections.unmodifiableList(combatChecks);
    }

    public List<Check> getMovementChecks() {
        return Collections.unmodifiableList(movementChecks);
    }

    public List<Check> getPlayerChecks() {
        return Collections.unmodifiableList(playerChecks);
    }

    /**
     * Get checks for a specific category within combat checks.
     * Returns empty list if category not found. O(1) lookup.
     */
    public List<Check> getCombatChecksByCategory(String category) {
        return combatByCategory.getOrDefault(category, Collections.emptyList());
    }

    /**
     * Get checks for a specific category within movement checks.
     */
    public List<Check> getMovementChecksByCategory(String category) {
        return movementByCategory.getOrDefault(category, Collections.emptyList());
    }

    /**
     * Get checks for a specific category within player checks.
     */
    public List<Check> getPlayerChecksByCategory(String category) {
        return playerByCategory.getOrDefault(category, Collections.emptyList());
    }

    public List<Check> getAllChecks() {
        List<Check> all = new ArrayList<>();
        all.addAll(combatChecks);
        all.addAll(movementChecks);
        all.addAll(playerChecks);
        return Collections.unmodifiableList(all);
    }

    public Check getCheck(String name) {
        for (Check check : getAllChecks()) {
            if (check.getFullCheckName().equalsIgnoreCase(name) || check.getCheckName().equalsIgnoreCase(name)) {
                return check;
            }
        }
        return null;
    }
}
