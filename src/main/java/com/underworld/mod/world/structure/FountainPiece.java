package com.underworld.mod.world.structure;

import com.underworld.mod.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class FountainPiece extends StructurePiece {
    private final BlockPos origin;

    public FountainPiece(BlockPos origin) {
        super(ModStructures.FOUNTAIN_PIECE.get(), 0,
                new BoundingBox(origin.getX() - 5, origin.getY() - 5, origin.getZ() - 5,
                        origin.getX() + 5, origin.getY() + 8, origin.getZ() + 5));
        this.origin = origin;
    }

    public FountainPiece(CompoundTag tag) {
        super(ModStructures.FOUNTAIN_PIECE.get(), tag);
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
                origin.getX() + 5, origin.getY() + 8, origin.getZ() + 5
        );
        build(level, level.getRandom(), box, origin);
    }

    private static void build(net.minecraft.world.level.ServerLevelAccessor level, RandomSource random,
                              BoundingBox box, BlockPos origin) {
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                if (Math.abs(dx) == 4 && Math.abs(dz) == 4) continue;
                BlockPos base = origin.offset(dx, 0, dz);
                BlockState floor = random.nextFloat() < 0.35F
                        ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState()
                        : Blocks.STONE_BRICKS.defaultBlockState();
                StructureBuildUtil.setIfInside(level, box, base, floor);
                StructureBuildUtil.fillColumnDown(level, box, base.below(), 5, Blocks.COBBLESTONE.defaultBlockState());

                for (int dy = 1; dy <= 6; dy++) {
                    StructureBuildUtil.setIfInside(level, box, origin.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
                }

                boolean rim = Math.abs(dx) == 3 || Math.abs(dz) == 3;
                boolean outside = Math.abs(dx) == 4 || Math.abs(dz) == 4;
                if (!outside && rim && !(Math.abs(dx) == 3 && Math.abs(dz) == 3)) {
                    BlockState wall = random.nextFloat() < 0.3F
                            ? Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                            : Blocks.STONE_BRICK_SLAB.defaultBlockState();
                    if (Math.abs(dx) == 3 && Math.abs(dz) == 0 || Math.abs(dz) == 3 && Math.abs(dx) == 0) {
                        wall = Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
                    }
                    StructureBuildUtil.setIfInside(level, box, origin.offset(dx, 1, dz), wall);
                } else if (!outside && (Math.abs(dx) == 3 && Math.abs(dz) == 3)) {
                    StructureBuildUtil.setIfInside(level, box, origin.offset(dx, 1, dz), Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
                    StructureBuildUtil.setIfInside(level, box, origin.offset(dx, 2, dz), Blocks.LANTERN.defaultBlockState());
                } else if (!outside && !rim) {
                    StructureBuildUtil.setIfInside(level, box, origin.offset(dx, 1, dz), Blocks.WATER.defaultBlockState());
                }
            }
        }

        // Central tiered fountain column
        for (int dy = 1; dy <= 4; dy++) {
            StructureBuildUtil.setIfInside(level, box, origin.offset(0, dy, 0),
                    dy == 4 ? Blocks.CHISELED_STONE_BRICKS.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState());
        }
        StructureBuildUtil.setIfInside(level, box, origin.offset(0, 5, 0), Blocks.WATER.defaultBlockState());

        // Mid-basin ledges
        int[][] mid = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] m : mid) {
            StructureBuildUtil.setIfInside(level, box, origin.offset(m[0], 3, m[1]), Blocks.STONE_BRICK_SLAB.defaultBlockState());
        }

        // Hidden sunken chest beside base
        StructureBuildUtil.placeChest(level, box, origin.offset(0, 0, -4), random, BuiltInLootTables.SIMPLE_DUNGEON);
    }
}
