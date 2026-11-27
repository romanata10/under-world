package com.underworld.mod.world.structure.lore;

import com.underworld.mod.world.UnderWorldSavedData;
import com.underworld.mod.world.structure.BrokenShaftPiece;
import com.underworld.mod.world.structure.ClockStatuePiece;
import com.underworld.mod.world.structure.ElongatedTreePiece;
import com.underworld.mod.world.structure.FountainPiece;
import com.underworld.mod.world.structure.GlitchClusterPiece;
import com.underworld.mod.world.structure.LeafMazePiece;
import com.underworld.mod.world.structure.OldVillageHousePiece;
import com.underworld.mod.world.structure.PowerPylonPiece;
import com.underworld.mod.world.structure.VoidShrinePiece;
import com.underworld.mod.world.structure.WheatFarmPiece;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class LoreStructureSpawner {
    public static final String[] LORE_STRUCTURE_IDS = {
            "hillside_house",
            "clan_base",
            "mario_pixel_art",
            "server_monument",
            "abandoned_treehouse",
            "secret_dugout",
            "old_spawn_hub",
            "villager_trading_hall",
            "computer_desk"
    };

    public static final String[] CLASSIC_STRUCTURE_IDS = {
            "fountain",
            "wheat_farm",
            "old_village_house",
            "leaf_maze",
            "broken_shaft",
            "clock_statue",
            "elongated_tree",
            "glitch_cluster",
            "void_shrine",
            "power_pylon"
    };

    private LoreStructureSpawner() {}

    public static boolean isLoreStructure(String id) {
        for (String s : LORE_STRUCTURE_IDS) {
            if (s.equals(id)) return true;
        }
        return false;
    }

    public static boolean placeById(ServerLevel level, String id, BlockPos pos) {
        return switch (id) {
            case "hillside_house" -> { LoreHillsideHousePiece.placeAt(level, pos); yield true; }
            case "clan_base" -> { LoreClanBasePiece.placeAt(level, pos); yield true; }
            case "mario_pixel_art" -> { LoreMarioPixelArtPiece.placeAt(level, pos); yield true; }
            case "server_monument" -> { LoreMonumentPiece.placeAt(level, pos); yield true; }
            case "abandoned_treehouse" -> { LoreTreehousePiece.placeAt(level, pos); yield true; }
            case "secret_dugout" -> { LoreDugoutPiece.placeAt(level, pos); yield true; }
            case "old_spawn_hub" -> { LoreSpawnHubPiece.placeAt(level, pos); yield true; }
            case "villager_trading_hall" -> { LoreTradingHallPiece.placeAt(level, pos); yield true; }
            case "computer_desk" -> { LoreComputerDeskPiece.placeAt(level, pos); yield true; }
            case "power_pylon" -> { PowerPylonPiece.placeAt(level, pos); yield true; }
            case "void_shrine" -> { VoidShrinePiece.placeAt(level, pos); yield true; }
            case "broken_shaft" -> {
                BlockPos deep = pos.getY() > 24 ? new BlockPos(pos.getX(), Math.max(level.getMinBuildHeight() + 8, pos.getY() - 18), pos.getZ()) : pos;
                BrokenShaftPiece.placeAt(level, deep);
                yield true;
            }
            case "fountain" -> { FountainPiece.placeAt(level, pos.below()); yield true; }
            case "glitch_cluster" -> { GlitchClusterPiece.placeAt(level, pos.below()); yield true; }
            case "elongated_tree" -> { ElongatedTreePiece.placeAt(level, pos.below()); yield true; }
            case "wheat_farm" -> { WheatFarmPiece.placeAt(level, pos.below()); yield true; }
            case "old_village_house" -> { OldVillageHousePiece.placeAt(level, pos.below()); yield true; }
            case "clock_statue" -> { ClockStatuePiece.placeAt(level, pos.below()); yield true; }
            case "leaf_maze" -> { LeafMazePiece.placeAt(level, pos.below()); yield true; }
            default -> false;
        };
    }

    /**
     * Dynamically spawns both classic Under World structures and new Server-Lore structures
     * during active survival gameplay at a discreet natural spot out of immediate reach.
     */
    @Nullable
    public static String trySpawnDynamicStructureAround(ServerLevel level, ServerPlayer anchor, UnderWorldSavedData data) {
        RandomSource random = level.getRandom();
        BlockPos spot = findSafeDynamicSpot(level, anchor, 38, 60);
        if (spot == null) {
            return null;
        }

        // Prioritize unseen lore structures first, then mix all classic + lore structures dynamically!
        List<String> unseenLore = new ArrayList<>();
        for (String id : LORE_STRUCTURE_IDS) {
            if (!data.hasLoreStructureSpawned(id)) {
                unseenLore.add(id);
            }
        }

        String chosenId;
        if (!unseenLore.isEmpty() && random.nextFloat() < 0.72F) {
            chosenId = unseenLore.get(random.nextInt(unseenLore.size()));
        } else if (random.nextBoolean()) {
            chosenId = LORE_STRUCTURE_IDS[random.nextInt(LORE_STRUCTURE_IDS.length)];
        } else {
            chosenId = CLASSIC_STRUCTURE_IDS[random.nextInt(CLASSIC_STRUCTURE_IDS.length)];
        }

        if (!placeById(level, chosenId, spot)) {
            return null;
        }
        if (isLoreStructure(chosenId)) {
            data.markLoreStructureSpawned(chosenId);
        }
        return chosenId;
    }

    @Nullable
    public static BlockPos findSafeDynamicSpot(ServerLevel level, ServerPlayer anchor, int minDist, int maxDist) {
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < 24; attempt++) {
            double ang = random.nextDouble() * Math.PI * 2.0D;
            double dist = minDist + random.nextDouble() * (maxDist - minDist);
            int tx = Mth.floor(anchor.getX() + Math.cos(ang) * dist);
            int tz = Mth.floor(anchor.getZ() + Math.sin(ang) * dist);

            if (!level.isLoaded(new BlockPos(tx, 64, tz))) {
                continue;
            }

            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, tx, tz);
            if (y <= level.getMinBuildHeight() + 8 || y >= level.getMaxBuildHeight() - 26) {
                continue;
            }

            BlockPos ground = new BlockPos(tx, y - 1, tz);
            if (!level.getFluidState(ground).isEmpty() || level.getFluidState(ground.above()).is(FluidTags.WATER)) {
                continue;
            }

            // Never build within 28 blocks of any player
            boolean tooCloseToPlayer = false;
            for (ServerPlayer p : level.players()) {
                if (p.blockPosition().distSqr(ground) < 28.0D * 28.0D) {
                    tooCloseToPlayer = true;
                    break;
                }
            }
            if (tooCloseToPlayer) {
                continue;
            }

            // Avoid overwriting existing player-built bases/chests/beds/glass
            if (containsPlayerBuiltBlocks(level, ground)) {
                continue;
            }

            return ground;
        }
        return null;
    }

    private static boolean containsPlayerBuiltBlocks(ServerLevel level, BlockPos center) {
        for (int dx = -5; dx <= 5; dx += 2) {
            for (int dz = -5; dz <= 5; dz += 2) {
                for (int dy = -2; dy <= 4; dy += 2) {
                    BlockState s = level.getBlockState(center.offset(dx, dy, dz));
                    if (s.is(Blocks.CHEST) || s.is(Blocks.TRAPPED_CHEST) || s.is(Blocks.FURNACE)
                            || s.is(Blocks.CRAFTING_TABLE) || s.is(Blocks.GLASS) || s.is(Blocks.GLASS_PANE)
                            || s.is(Blocks.TORCH) || s.is(Blocks.STONE_BRICKS) || s.is(Blocks.OAK_PLANKS)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
