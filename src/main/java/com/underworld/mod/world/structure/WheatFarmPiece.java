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
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class WheatFarmPiece extends StructurePiece {
    private final BlockPos origin;

    public WheatFarmPiece(BlockPos origin) {
        super(ModStructures.WHEAT_FARM_PIECE.get(), 0,
                new BoundingBox(origin.getX() - 6, origin.getY() - 5, origin.getZ() - 6,
                        origin.getX() + 6, origin.getY() + 6, origin.getZ() + 6));
        this.origin = origin;
    }

    public WheatFarmPiece(CompoundTag tag) {
        super(ModStructures.WHEAT_FARM_PIECE.get(), tag);
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
                origin.getX() - 6, origin.getY() - 5, origin.getZ() - 6,
                origin.getX() + 6, origin.getY() + 6, origin.getZ() + 6
        );
        build(level, level.getRandom(), box, origin);
    }

    private static void build(ServerLevelAccessor level, RandomSource random, BoundingBox box, BlockPos origin) {
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState fence = Blocks.OAK_FENCE.defaultBlockState();
        BlockState wetFarm = Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7);

        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                boolean border = Math.abs(dx) == 5 || Math.abs(dz) == 5;
                boolean corner = Math.abs(dx) == 5 && Math.abs(dz) == 5;
                boolean gate = (dx == 0 && dz == 5);
                boolean waterRow = !border && dx == 0;

                BlockPos floorPos = origin.offset(dx, 0, dz);
                StructureBuildUtil.fillColumnDown(level, box, floorPos.below(), 5, Blocks.DIRT.defaultBlockState());
                for (int dy = 1; dy <= 5; dy++) {
                    StructureBuildUtil.setIfInside(level, box, origin.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
                }

                if (corner) {
                    StructureBuildUtil.setIfInside(level, box, floorPos, log);
                    StructureBuildUtil.setIfInside(level, box, origin.offset(dx, 1, dz), log);
                    StructureBuildUtil.setIfInside(level, box, origin.offset(dx, 2, dz), Blocks.TORCH.defaultBlockState());
                } else if (border) {
                    StructureBuildUtil.setIfInside(level, box, floorPos, Blocks.GRASS_BLOCK.defaultBlockState());
                    StructureBuildUtil.setIfInside(level, box, origin.offset(dx, 1, dz),
                            gate ? Blocks.OAK_FENCE_GATE.defaultBlockState() : fence);
                } else if (waterRow) {
                    StructureBuildUtil.setIfInside(level, box, floorPos, Blocks.WATER.defaultBlockState());
                    if (Math.abs(dz) <= 1) {
                        StructureBuildUtil.setIfInside(level, box, origin.offset(dx, 1, dz), Blocks.OAK_SLAB.defaultBlockState());
                    }
                } else {
                    StructureBuildUtil.setIfInside(level, box, floorPos, wetFarm);
                    int age = random.nextFloat() < 0.72F ? 7 : 4 + random.nextInt(3);
                    StructureBuildUtil.setIfInside(level, box, origin.offset(dx, 1, dz),
                            Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, age));
                }
            }
        }

        // Hay bales, composter & farm supply chest
        StructureBuildUtil.setIfInside(level, box, origin.offset(-4, 0, -4), Blocks.OAK_PLANKS.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, origin.offset(-4, 1, -4), Blocks.HAY_BLOCK.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, origin.offset(-3, 1, -4), Blocks.HAY_BLOCK.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, origin.offset(-4, 2, -4), Blocks.HAY_BLOCK.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, origin.offset(4, 0, -4), Blocks.OAK_PLANKS.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, origin.offset(4, 1, -4), Blocks.COMPOSTER.defaultBlockState());
        StructureBuildUtil.placeChest(level, box, origin.offset(3, 1, -4), random, BuiltInLootTables.VILLAGE_PLAINS_HOUSE);
    }
}
