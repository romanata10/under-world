package com.underworld.mod.world.structure;

import com.underworld.mod.registry.ModBlocks;
import com.underworld.mod.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class GlitchClusterPiece extends StructurePiece {
    private final BlockPos origin;

    public GlitchClusterPiece(BlockPos origin) {
        super(ModStructures.GLITCH_CLUSTER_PIECE.get(), 0,
                new BoundingBox(origin.getX() - 5, origin.getY() - 5, origin.getZ() - 5,
                        origin.getX() + 5, origin.getY() + 10, origin.getZ() + 5));
        this.origin = origin;
    }

    public GlitchClusterPiece(CompoundTag tag) {
        super(ModStructures.GLITCH_CLUSTER_PIECE.get(), tag);
        this.origin = new BlockPos(tag.getInt("OX"), tag.getInt("OY"), tag.getInt("OZ"));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("OX", this.origin.getX());
        tag.putInt("OY", this.origin.getY());
        tag.putInt("OZ", this.origin.getZ());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
        build(level, random, box, this.origin);
    }

    public static void placeAt(ServerLevel level, BlockPos origin) {
        BoundingBox box = new BoundingBox(
                origin.getX() - 5, origin.getY() - 5, origin.getZ() - 5,
                origin.getX() + 5, origin.getY() + 10, origin.getZ() + 5
        );
        build(level, level.getRandom(), box, origin);
    }

    private static void build(ServerLevelAccessor level, RandomSource random, BoundingBox box, BlockPos origin) {
        BlockState glitch = ModBlocks.GLITCH_BLOCK.get().defaultBlockState();
        BlockState errorGround = ModBlocks.ERROR_GROUND.get().defaultBlockState();
        BlockState obsidian = Blocks.CRYING_OBSIDIAN.defaultBlockState();
        BlockState concrete = Blocks.BLACK_CONCRETE.defaultBlockState();

        // Corrupted fracture crater pad
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                int distSq = dx * dx + dz * dz;
                if (distSq > 18) continue;
                BlockPos base = origin.offset(dx, 0, dz);
                BlockState floor = distSq <= 4
                        ? glitch
                        : (random.nextFloat() < 0.38F ? errorGround : (random.nextFloat() < 0.5F ? obsidian : concrete));
                StructureBuildUtil.setIfInside(level, box, base, floor);
                StructureBuildUtil.fillColumnDown(level, box, base.below(), 5, concrete);

                for (int dy = 1; dy <= 9; dy++) {
                    StructureBuildUtil.setIfInside(level, box, origin.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
                }
            }
        }

        // Jagged rising glitch block pillars flickering with every texture in the game
        int[][] pillars = {
                {0, 0, 5}, {-2, -1, 4}, {2, 1, 4}, {-1, 2, 3}, {2, -2, 3}, {-3, 1, 2}
        };
        for (int[] p : pillars) {
            int h = p[2];
            for (int dy = 1; dy <= h; dy++) {
                // Leave occasional broken air gaps inside pillars!
                if (dy == 2 && h >= 4 && random.nextFloat() < 0.45F) {
                    continue;
                }
                BlockState state = (dy == h || random.nextFloat() < 0.72F) ? glitch : errorGround;
                StructureBuildUtil.setIfInside(level, box, origin.offset(p[0], dy, p[1]), state);
            }
        }

        // Floating suspended glitch shards & floating water pocket overhead
        StructureBuildUtil.setIfInside(level, box, origin.offset(0, 7, 0), glitch);
        StructureBuildUtil.setIfInside(level, box, origin.offset(1, 8, -1), glitch);
        StructureBuildUtil.setIfInside(level, box, origin.offset(-2, 7, 2), glitch);
        StructureBuildUtil.setIfInside(level, box, origin.offset(-1, 6, -2), Blocks.WATER.defaultBlockState());

        StructureBuildUtil.placeChest(level, box, origin.offset(1, 1, 0), random, BuiltInLootTables.SIMPLE_DUNGEON);
    }
}
