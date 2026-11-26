package com.underworld.mod.block;

import com.underworld.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class FunFlowerBlock extends BaseEntityBlock {
    public enum FlowerVariant {
        YELLOW_DAISY("fun_yellow_flower", false),
        BLUE_DAISY("fun_blue_flower", false),
        FLAT_RED("fun_flat_red_flower", true),
        FLAT_BLUE("fun_flat_blue_flower", true),
        FLAT_WHITE("fun_flat_white_flower", true);

        private final String assetName;
        private final boolean flat;

        FlowerVariant(String assetName, boolean flat) {
            this.assetName = assetName;
            this.flat = flat;
        }

        public String getAssetName() {
            return this.assetName;
        }

        public boolean isFlat() {
            return this.flat;
        }
    }

    private static final VoxelShape TALL_SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);
    private static final VoxelShape FLAT_SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 5.0D, 14.0D);

    private final FlowerVariant variant;

    public FunFlowerBlock(FlowerVariant variant, Properties properties) {
        super(properties);
        this.variant = variant;
    }

    public FlowerVariant getVariant() {
        return this.variant;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.variant.isFlat() ? FLAT_SHAPE : TALL_SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(ModBlocks.FUN_GRASS_BLOCK.get())
                || below.is(ModBlocks.FUN_DIRT.get())
                || below.is(Blocks.GRASS_BLOCK)
                || below.is(Blocks.DIRT);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FunFlowerBlockEntity(pos, state);
    }
}
