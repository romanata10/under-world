package com.underworld.mod.block;

import com.underworld.mod.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GlitchBlockEntity extends BlockEntity {
    public GlitchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GLITCH_BLOCK.get(), pos, state);
    }
}
