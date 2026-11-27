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

public class ClockStatuePiece extends StructurePiece {
    private final BlockPos origin;

    public ClockStatuePiece(BlockPos origin) {
        super(ModStructures.CLOCK_STATUE_PIECE.get(), 0,
                new BoundingBox(origin.getX() - 4, origin.getY() - 5, origin.getZ() - 4,
                        origin.getX() + 4, origin.getY() + 14, origin.getZ() + 4));
        this.origin = origin;
    }

    public ClockStatuePiece(CompoundTag tag) {
        super(ModStructures.CLOCK_STATUE_PIECE.get(), tag);
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
                origin.getX() - 4, origin.getY() - 5, origin.getZ() - 4,
                origin.getX() + 4, origin.getY() + 14, origin.getZ() + 4
        );
        build(level, level.getRandom(), box, origin);
    }

    private static void build(ServerLevelAccessor level, RandomSource random, BoundingBox box, BlockPos origin) {
        BlockState rim = ModBlocks.CLOCK_RIM_BLOCK.get().defaultBlockState();
        BlockState face = ModBlocks.CLOCK_FACE_BLOCK.get().defaultBlockState();
        BlockState brick = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        BlockState chiseled = Blocks.CHISELED_DEEPSLATE.defaultBlockState();

        // Stepped 5x5 pedestal & cleared air column
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos base = origin.offset(dx, 0, dz);
                StructureBuildUtil.setIfInside(level, box, base, brick);
                StructureBuildUtil.fillColumnDown(level, box, base.below(), 5, brick);
                for (int dy = 1; dy <= 13; dy++) {
                    StructureBuildUtil.setIfInside(level, box, origin.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
                }
                if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                    StructureBuildUtil.setIfInside(level, box, origin.offset(dx, 1, dz), chiseled);
                }
            }
        }

        // Corner soul lanterns on pedestal
        for (int[] c : new int[][]{{-2, -2}, {2, -2}, {-2, 2}, {2, 2}}) {
            StructureBuildUtil.setIfInside(level, box, origin.offset(c[0], 1, c[1]), Blocks.SOUL_LANTERN.defaultBlockState());
        }

        // Tall Grandfather Clock tower body with hanging gold pendulum window
        for (int dy = 2; dy <= 7; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    boolean isFrontOrBackHollow = (dx == 0 && Math.abs(dz) == 1 && dy >= 3 && dy <= 6);
                    boolean isCenterHollow = (dx == 0 && dz == 0 && dy >= 3 && dy <= 6);
                    if (isFrontOrBackHollow) {
                        StructureBuildUtil.setIfInside(level, box, origin.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
                    } else if (isCenterHollow) {
                        BlockState pend = dy == 3 ? Blocks.GOLD_BLOCK.defaultBlockState() : Blocks.CHAIN.defaultBlockState();
                        StructureBuildUtil.setIfInside(level, box, origin.offset(dx, dy, dz), pend);
                    } else {
                        StructureBuildUtil.setIfInside(level, box, origin.offset(dx, dy, dz), rim);
                    }
                }
            }
        }

        // Giant 5x5 Circular Analog Clock Face at dy = 8..12!
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = 8; dy <= 12; dy++) {
                int relY = dy - 10;
                if (Math.abs(dx) == 2 && Math.abs(relY) == 2) continue;
                boolean outerRing = Math.abs(dx) == 2 || Math.abs(relY) == 2;
                boolean isHand = (dx == 0 && relY >= 0 && relY <= 1) || (relY == 0 && dx >= 0 && dx <= 1);
                for (int dz = -1; dz <= 1; dz++) {
                    BlockState blk;
                    if (dz == 0 || outerRing || isHand) {
                        blk = rim;
                    } else {
                        blk = face;
                    }
                    StructureBuildUtil.setIfInside(level, box, origin.offset(dx, dy, dz), blk);
                }
            }
        }

        StructureBuildUtil.placeChest(level, box, origin.offset(0, 1, 2), random, BuiltInLootTables.SIMPLE_DUNGEON);
    }
}
