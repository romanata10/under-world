package com.underworld.mod.block;

import com.underworld.mod.event.UnderWorldEventDirector;
import com.underworld.mod.world.dimension.BrowserChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class DownloadButtonBlock extends Block {
    public DownloadButtonBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        trigger(level, pos, entity);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        trigger(level, pos, entity);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        trigger(level, pos, player);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void trigger(Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel && entity instanceof ServerPlayer player) {
            BrowserChunkGenerator.activateDownloadTrap(serverLevel, pos, player);
            UnderWorldEventDirector.triggerDotComTrap(serverLevel, pos, player);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            level.addParticle(
                    ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + random.nextDouble(),
                    pos.getY() + 1.05D,
                    pos.getZ() + random.nextDouble(),
                    0.0D, 0.05D, 0.0D
            );
        }
    }
}
