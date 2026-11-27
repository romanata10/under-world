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

public class TimePortalBlock extends Block {
    public TimePortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player && !player.isOnPortalCooldown()) {
            player.setPortalCooldown(100);
            level.playSound(null, pos, ModSounds.TIME_WARP.get(), SoundSource.AMBIENT, 1.3F, 1.1F);
            if (ModDimensions.isTime(level)) {
                ModDimensions.returnToOverworldFromTime(player);
            } else {
                ModDimensions.sendToTime(player);
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            level.addParticle(
                    ParticleTypes.END_ROD,
                    pos.getX() + random.nextDouble(),
                    pos.getY() + random.nextDouble(),
                    pos.getZ() + random.nextDouble(),
                    0.0D, 0.03D, 0.0D
            );
        }
    }
}
