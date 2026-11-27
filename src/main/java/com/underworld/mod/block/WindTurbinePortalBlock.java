package com.underworld.mod.block;

import com.underworld.mod.registry.ModDimensions;
import com.underworld.mod.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class WindTurbinePortalBlock extends Block {
    public WindTurbinePortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        tryTeleport(level, pos, entity);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        tryTeleport(level, pos, entity);
    }

    private static void tryTeleport(Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player && !player.isOnPortalCooldown()) {
            player.setPortalCooldown(100);
            level.playSound(null, pos, ModSounds.TURBINE_WHOOSH.get(), SoundSource.AMBIENT, 1.4F, 0.95F);
            ModDimensions.sendToWindOcean(player);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            level.addParticle(
                    ParticleTypes.CLOUD,
                    pos.getX() + random.nextDouble(),
                    pos.getY() + 0.5D + random.nextDouble(),
                    pos.getZ() + random.nextDouble(),
                    0.0D, 0.04D, 0.0D
            );
        }
    }
}
