package com.underworld.mod.block;

import com.underworld.mod.event.UnderWorldEventDirector;
import com.underworld.mod.registry.ModDimensions;
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

public class MazeTeleporterBlock extends Block {
    public MazeTeleporterBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer && !serverPlayer.isOnPortalCooldown()) {
            serverPlayer.setPortalCooldown(80);
            ModDimensions.returnToOverworld(serverPlayer);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel
                && entity instanceof ServerPlayer serverPlayer && !serverPlayer.isOnPortalCooldown()) {
            serverPlayer.setPortalCooldown(80);
            java.util.UUID pid = serverPlayer.getUUID();
            UnderWorldEventDirector.schedule(1, () -> {
                ServerPlayer p = serverLevel.getServer().getPlayerList().getPlayer(pid);
                if (p != null && p.isAlive() && p.level() == serverLevel) {
                    ModDimensions.returnToOverworld(p);
                }
            });
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(2) == 0) {
            level.addParticle(
                    ParticleTypes.END_ROD,
                    pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.7D,
                    pos.getY() + 1.1D,
                    pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.7D,
                    0.0D, 0.04D, 0.0D
            );
        }
    }
}
