package com.underworld.mod.world.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public final class StructureBuildUtil {
    private StructureBuildUtil() {}

    public static void setIfInside(ServerLevelAccessor level, BoundingBox box, BlockPos pos, BlockState state) {
        if (box.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }

    public static void fillColumnDown(ServerLevelAccessor level, BoundingBox box, BlockPos startPos, int maxDepth, BlockState fill) {
        for (int d = 0; d < maxDepth; d++) {
            BlockPos p = startPos.below(d);
            if (!box.isInside(p)) {
                continue;
            }
            if (p.getY() <= level.getMinBuildHeight() + 1) {
                break;
            }
            BlockState existing = level.getBlockState(p);
            if (!existing.isAir() && existing.getFluidState().isEmpty() && d > 0) {
                break;
            }
            level.setBlock(p, fill, 2);
        }
    }

    public static void placeChest(ServerLevelAccessor level, BoundingBox box, BlockPos pos, RandomSource random, ResourceLocation lootTable) {
        if (box.isInside(pos)) {
            level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 2);
            if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
                chest.setLootTable(lootTable, random.nextLong());
            }
        }
    }
}
