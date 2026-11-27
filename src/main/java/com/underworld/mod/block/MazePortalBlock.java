package com.underworld.mod.block;

import com.underworld.mod.event.UnderWorldEventDirector;
import com.underworld.mod.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class MazePortalBlock extends Block {
    public MazePortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel
                && entity instanceof ServerPlayer player && !player.isOnPortalCooldown()) {
            player.setPortalCooldown(80);
            java.util.UUID pid = player.getUUID();
            boolean inMaze = ModDimensions.isMaze(level);
            UnderWorldEventDirector.schedule(1, () -> {
                ServerPlayer p = serverLevel.getServer().getPlayerList().getPlayer(pid);
                if (p != null && p.isAlive() && p.level() == serverLevel) {
                    if (inMaze) {
                        ModDimensions.returnToOverworld(p);
                    } else {
                        ModDimensions.sendToMaze(p);
                    }
                }
            });
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            level.addParticle(
                    ParticleTypes.CRIMSON_SPORE,
                    pos.getX() + random.nextDouble(),
                    pos.getY() + random.nextDouble(),
                    pos.getZ() + random.nextDouble(),
                    0.0D, 0.02D, 0.0D
            );
        }
    }
}
