package com.underworld.mod.world.structure;

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
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class ElongatedTreePiece extends StructurePiece {
    private final BlockPos origin;

    public ElongatedTreePiece(BlockPos origin) {
        super(ModStructures.ELONGATED_TREE_PIECE.get(), 0,
                new BoundingBox(origin.getX() - 4, origin.getY() - 3, origin.getZ() - 4,
                        origin.getX() + 4, origin.getY() + 34, origin.getZ() + 4));
        this.origin = origin;
    }

    public ElongatedTreePiece(CompoundTag tag) {
        super(ModStructures.ELONGATED_TREE_PIECE.get(), tag);
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
                origin.getX() - 4, origin.getY() - 3, origin.getZ() - 4,
                origin.getX() + 4, origin.getY() + 34, origin.getZ() + 4
        );
        build(level, level.getRandom(), box, origin);
    }

    private static void build(ServerLevelAccessor level, RandomSource random, BoundingBox box, BlockPos origin) {
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        int trunkHeight = 23 + random.nextInt(8);

        // Ground root flare & fill beneath base
        StructureBuildUtil.fillColumnDown(level, box, origin, 4, Blocks.DIRT.defaultBlockState());
        for (int[] root : new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}}) {
            StructureBuildUtil.setIfInside(level, box, origin.offset(root[0], 1, root[1]), log);
            StructureBuildUtil.fillColumnDown(level, box, origin.offset(root[0], 0, root[1]), 4, Blocks.DIRT.defaultBlockState());
        }

        // Unnaturally tall skinny oak trunk shooting straight into the sky
        for (int dy = 1; dy <= trunkHeight; dy++) {
            StructureBuildUtil.setIfInside(level, box, origin.offset(0, dy, 0), log);
            // Odd horizontal twig jutting way up high
            if (dy == trunkHeight - 8) {
                StructureBuildUtil.setIfInside(level, box, origin.offset(1, dy, 0), log);
                StructureBuildUtil.setIfInside(level, box, origin.offset(2, dy, 0), log);
                StructureBuildUtil.setIfInside(level, box, origin.offset(2, dy + 1, 0), leaves);
            }
            if (dy == trunkHeight - 5) {
                StructureBuildUtil.setIfInside(level, box, origin.offset(-1, dy, 1), log);
                StructureBuildUtil.setIfInside(level, box, origin.offset(-2, dy + 1, 1), leaves);
            }
        }

        // Vertically stretched, weirdly tall narrow oak-leaf canopy at the top
        int canopyStart = trunkHeight - 6;
        for (int dy = 0; dy <= 9; dy++) {
            int rad = dy < 6 ? 2 : 1;
            for (int dx = -rad; dx <= rad; dx++) {
                for (int dz = -rad; dz <= rad; dz++) {
                    if (dx == 0 && dz == 0 && dy < 7) continue;
                    if (Math.abs(dx) == 2 && Math.abs(dz) == 2 && random.nextFloat() < 0.7F) continue;
                    StructureBuildUtil.setIfInside(level, box, origin.offset(dx, canopyStart + dy, dz), leaves);
                }
            }
        }
        StructureBuildUtil.setIfInside(level, box, origin.offset(0, canopyStart + 10, 0), leaves);
    }
}
