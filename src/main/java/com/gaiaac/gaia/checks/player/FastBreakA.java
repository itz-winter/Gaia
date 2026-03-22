package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * FastBreak (A) - Detects breaking blocks faster than vanilla limits.
 *
 * Tracks digging start time and compares against the expected break duration
 * based on block hardness, tool type, tool enchantments, and potion effects.
 * Uses a balance system (like Grim) so a single borderline break doesn't flag,
 * but sustained fast-breaking does.
 *
 * Also enforces a minimum delay between consecutive block breaks (~300ms for
 * non-instant blocks), which catches Nuker-style cheats that break many blocks
 * in rapid succession.
 */
public class FastBreakA extends Check {

    private long startDigTime = 0;
    private long lastFinishBreakTime = 0;
    private double breakSpeedBalance = 0;
    private double breakDelayBalance = 0;

    public FastBreakA(GaiaPlugin plugin) { super(plugin, "FastBreak", "A", "fastbreak", true, 8); }

    @Override
    public void handle(Player player, PlayerData data) {
        // This check is event-driven via digging packet data stored on PlayerData.
        // PacketManager calls this on PLAYER_DIGGING packets.
        // We use the digging state fields from PlayerData.

        int action = data.getDiggingAction(); // 0 = START, 2 = FINISH, 1 = CANCEL, -1 = none

        if (action == 0) {
            // START_DIGGING — record when the player began breaking
            startDigTime = System.currentTimeMillis();
            return;
        }

        if (action == 1) {
            // CANCELLED_DIGGING — player stopped, reset
            startDigTime = 0;
            return;
        }

        if (action == 2 && startDigTime > 0) {
            // FINISHED_DIGGING — check if they broke it too fast
            if (recentlyTeleported(data) || recentlyJoined(data)) return;
            if (isLowTPS()) return;

            // Creative mode can insta-break everything
            if (player.getGameMode() == GameMode.CREATIVE) {
                lastFinishBreakTime = System.currentTimeMillis();
                startDigTime = 0;
                return;
            }

            long now = System.currentTimeMillis();
            long realTime = now - startDigTime;

            // Get expected break time using server-side calculation
            // We schedule this from the main thread context (FastBreak is called from PLAYER_DIGGING packet handler)
            // For safety, use the cached block data approach
            Block targetBlock = data.getLastDigBlock();
            if (targetBlock == null) {
                startDigTime = 0;
                return;
            }

            double expectedTicks = calculateExpectedBreakTicks(player, targetBlock);

            // Instant-break blocks (hardness 0 or expectedTicks <= 0) are always valid
            if (expectedTicks <= 0) {
                lastFinishBreakTime = now;
                startDigTime = 0;
                return;
            }

            double expectedMs = expectedTicks * 50.0; // Convert ticks to milliseconds

            // How much faster was the player than expected?
            double diff = expectedMs - realTime;

            // --- Break speed balance ---
            // Lenient: allow up to 50ms faster without increasing balance (accounts for latency)
            if (diff < 50) {
                breakSpeedBalance *= 0.9; // Reward: slowly drain balance
            } else {
                breakSpeedBalance += diff;
            }

            // Clamp balance — don't go too negative or too high
            breakSpeedBalance = Math.max(-2000, Math.min(breakSpeedBalance, 5000));

            if (breakSpeedBalance > 1000) {
                double buffer = data.addBuffer("fastbreak_a_speed_buffer", 1);
                if (buffer > 3) {
                    flag(player, data, String.format("speed diff=%.0fms balance=%.0f block=%s",
                            diff, breakSpeedBalance, targetBlock.getType().name()));
                    data.setBuffer("fastbreak_a_speed_buffer", 0);
                }
            } else {
                data.decreaseBuffer("fastbreak_a_speed_buffer", 0.5);
            }

            // --- Break delay balance (Nuker detection) ---
            // Non-instant blocks should have at least ~300ms between finishes
            if (lastFinishBreakTime > 0) {
                long breakDelay = now - lastFinishBreakTime;

                if (breakDelay >= 275) {
                    breakDelayBalance *= 0.9;
                } else {
                    breakDelayBalance += (300 - breakDelay);
                }

                breakDelayBalance = Math.max(-2000, Math.min(breakDelayBalance, 5000));

                if (breakDelayBalance > 1000) {
                    double buffer = data.addBuffer("fastbreak_a_delay_buffer", 1);
                    if (buffer > 3) {
                        flag(player, data, String.format("nuker delay=%dms balance=%.0f",
                                breakDelay, breakDelayBalance));
                        data.setBuffer("fastbreak_a_delay_buffer", 0);
                    }
                } else {
                    data.decreaseBuffer("fastbreak_a_delay_buffer", 0.5);
                }
            }

            lastFinishBreakTime = now;
            startDigTime = 0;
        }
    }

    /**
     * Calculates the expected number of ticks to break a block, accounting for
     * tool type, efficiency enchantment, haste/mining fatigue, and being underwater.
     * Based on the Minecraft wiki: https://minecraft.wiki/w/Breaking#Speed
     *
     * Returns 0 for instant-break blocks.
     */
    private double calculateExpectedBreakTicks(Player player, Block block) {
        Material blockType = block.getType();
        float hardness = blockType.getHardness();

        // Unbreakable or instant break
        if (hardness < 0) return -1; // unbreakable (e.g. bedrock)
        if (hardness == 0) return 0; // instant break (e.g. tall grass)

        ItemStack tool = player.getInventory().getItemInMainHand();
        float speedMultiplier = getToolSpeedMultiplier(tool, blockType);

        // Efficiency enchantment
        if (speedMultiplier > 1.0f && tool != null) {
            int effLevel = tool.getEnchantmentLevel(Enchantment.EFFICIENCY);
            if (effLevel > 0) {
                speedMultiplier += (effLevel * effLevel + 1);
            }
        }

        // Haste effect
        int hasteAmplifier = data_getEffectAmplifier(player, PotionEffectType.HASTE);
        if (hasteAmplifier >= 0) {
            speedMultiplier *= (1.0f + (hasteAmplifier + 1) * 0.2f);
        }

        // Conduit Power acts like Haste
        int conduitAmplifier = data_getEffectAmplifier(player, PotionEffectType.CONDUIT_POWER);
        if (conduitAmplifier >= 0) {
            int combined = Math.max(hasteAmplifier, conduitAmplifier);
            if (conduitAmplifier > hasteAmplifier) {
                speedMultiplier *= (1.0f + (combined + 1) * 0.2f);
            }
        }

        // Mining Fatigue
        int fatigueAmplifier = data_getEffectAmplifier(player, PotionEffectType.MINING_FATIGUE);
        if (fatigueAmplifier >= 0) {
            switch (fatigueAmplifier) {
                case 0: speedMultiplier *= 0.3f; break;
                case 1: speedMultiplier *= 0.09f; break;
                case 2: speedMultiplier *= 0.0027f; break;
                default: speedMultiplier *= 0.00081f; break;
            }
        }

        // In water without Aqua Affinity
        if (player.isInWater()) {
            ItemStack helmet = player.getInventory().getHelmet();
            boolean hasAquaAffinity = helmet != null && helmet.getEnchantmentLevel(Enchantment.AQUA_AFFINITY) > 0;
            if (!hasAquaAffinity) {
                speedMultiplier /= 5.0f;
            }
        }

        // Not on ground
        if (!player.isOnGround()) {
            speedMultiplier /= 5.0f;
        }

        // Can harvest check (simplified — tool matches block type)
        boolean canHarvest = canHarvestBlock(tool, blockType);

        float damage = speedMultiplier / hardness;
        damage /= canHarvest ? 30.0f : 100.0f;

        // Instant break?
        if (damage >= 1.0f) return 0;

        return Math.ceil(1.0 / damage);
    }

    private int data_getEffectAmplifier(Player player, PotionEffectType type) {
        PotionEffect effect = player.getPotionEffect(type);
        return effect != null ? effect.getAmplifier() : -1;
    }

    /**
     * Gets the base speed multiplier for a tool against a block type.
     * This is a simplified version — covers the common tool/block combos.
     */
    @SuppressWarnings("deprecation")
    private float getToolSpeedMultiplier(ItemStack tool, Material blockType) {
        if (tool == null || tool.getType() == Material.AIR) return 1.0f;

        String toolName = tool.getType().name();
        String blockName = blockType.name();

        // Swords have 1.5x against cobwebs
        if (toolName.contains("SWORD") && blockName.equals("COBWEB")) return 15.0f;
        // Shears
        if (toolName.equals("SHEARS")) {
            if (blockName.contains("WOOL") || blockName.contains("LEAF") || blockName.contains("LEAVES")) return 5.0f;
            if (blockName.equals("COBWEB") || blockName.equals("VINE")) return 15.0f;
        }

        // Check if the tool material matches the block category
        float tierSpeed = getToolTierSpeed(toolName);

        if (tierSpeed > 1.0f) {
            // Check if the tool is effective against this block
            if (isToolEffective(toolName, blockName)) {
                return tierSpeed;
            }
        }

        return 1.0f;
    }

    private float getToolTierSpeed(String toolName) {
        if (toolName.contains("WOODEN") || toolName.contains("WOOD")) return 2.0f;
        if (toolName.contains("STONE")) return 4.0f;
        if (toolName.contains("IRON")) return 6.0f;
        if (toolName.contains("DIAMOND")) return 8.0f;
        if (toolName.contains("NETHERITE")) return 9.0f;
        if (toolName.contains("GOLDEN") || toolName.contains("GOLD")) return 12.0f;
        return 1.0f;
    }

    private boolean isToolEffective(String toolName, String blockName) {
        if (toolName.contains("PICKAXE")) {
            return blockName.contains("STONE") || blockName.contains("ORE") || blockName.contains("IRON")
                    || blockName.contains("GOLD") || blockName.contains("DIAMOND") || blockName.contains("NETHERITE")
                    || blockName.contains("BRICK") || blockName.contains("OBSIDIAN") || blockName.contains("COPPER")
                    || blockName.contains("DEEPSLATE") || blockName.contains("BASALT") || blockName.contains("TERRACOTTA")
                    || blockName.contains("CONCRETE") || blockName.contains("AMETHYST") || blockName.contains("DRIPSTONE")
                    || blockName.contains("PRISMARINE") || blockName.contains("PURPUR") || blockName.contains("QUARTZ")
                    || blockName.contains("SANDSTONE") || blockName.contains("BLACKSTONE") || blockName.contains("ANDESITE")
                    || blockName.contains("DIORITE") || blockName.contains("GRANITE") || blockName.contains("TUFF")
                    || blockName.contains("CALCITE") || blockName.contains("COBBLESTONE") || blockName.contains("ICE")
                    || blockName.contains("LANTERN") || blockName.contains("RAIL") || blockName.contains("CHAIN")
                    || blockName.contains("BREWING") || blockName.contains("CAULDRON") || blockName.contains("HOPPER")
                    || blockName.contains("PISTON") || blockName.contains("DISPENSER") || blockName.contains("DROPPER")
                    || blockName.contains("FURNACE") || blockName.contains("SMOKER") || blockName.contains("BLAST");
        }
        if (toolName.contains("AXE") && !toolName.contains("PICKAXE")) {
            return blockName.contains("WOOD") || blockName.contains("LOG") || blockName.contains("PLANK")
                    || blockName.contains("FENCE") || blockName.contains("GATE") || blockName.contains("SIGN")
                    || blockName.contains("DOOR") && !blockName.contains("IRON") || blockName.contains("TRAPDOOR")
                    || blockName.contains("BOOKSHELF") || blockName.contains("CHEST") || blockName.contains("BARREL")
                    || blockName.contains("CRAFTING") || blockName.contains("LADDER") || blockName.contains("CAMPFIRE")
                    || blockName.contains("STEM") || blockName.contains("HYPHAE") || blockName.contains("BAMBOO")
                    || blockName.contains("MUSHROOM") || blockName.contains("PUMPKIN") || blockName.contains("MELON")
                    || blockName.contains("COCOA") || blockName.contains("JUKEBOX") || blockName.contains("BEE");
        }
        if (toolName.contains("SHOVEL")) {
            return blockName.contains("DIRT") || blockName.contains("GRASS") || blockName.contains("SAND")
                    || blockName.contains("GRAVEL") || blockName.contains("SNOW") || blockName.contains("CLAY")
                    || blockName.contains("FARMLAND") || blockName.contains("SOUL") || blockName.contains("MUD")
                    || blockName.contains("MYCELIUM") || blockName.contains("PODZOL") || blockName.contains("ROOTED")
                    || blockName.contains("CONCRETE_POWDER");
        }
        if (toolName.contains("HOE")) {
            return blockName.contains("LEAVES") || blockName.contains("LEAF") || blockName.contains("HAY")
                    || blockName.contains("SPONGE") || blockName.contains("NETHER_WART_BLOCK")
                    || blockName.contains("WARPED_WART") || blockName.contains("SHROOMLIGHT")
                    || blockName.contains("SCULK") || blockName.contains("MOSS") || blockName.contains("TARGET");
        }
        return false;
    }

    private boolean canHarvestBlock(ItemStack tool, Material blockType) {
        // Most blocks can be harvested by hand, but some require specific tool types
        String blockName = blockType.name();

        // Blocks that require a pickaxe (stone/ore variants)
        if (blockName.contains("STONE") || blockName.contains("ORE") || blockName.contains("OBSIDIAN")
                || blockName.contains("DIAMOND_BLOCK") || blockName.contains("IRON_BLOCK")
                || blockName.contains("GOLD_BLOCK") || blockName.contains("EMERALD_BLOCK")
                || blockName.contains("LAPIS_BLOCK") || blockName.contains("REDSTONE_BLOCK")
                || blockName.contains("NETHERITE") || blockName.contains("COPPER_BLOCK")
                || blockName.contains("DEEPSLATE") || blockName.contains("COBBLESTONE")
                || blockName.contains("BRICK") || blockName.contains("PRISMARINE")
                || blockName.contains("PURPUR") || blockName.contains("END_STONE")
                || blockName.contains("BASALT") || blockName.contains("BLACKSTONE")) {
            return tool != null && tool.getType().name().contains("PICKAXE");
        }

        return true; // Most blocks don't require a specific tool to harvest
    }
}
