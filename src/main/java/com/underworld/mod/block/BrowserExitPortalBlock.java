package com.underworld.mod.block;

import com.underworld.mod.event.UnderWorldEventDirector;
import com.underworld.mod.registry.ModDimensions;
import com.underworld.mod.registry.ModItems;
import com.underworld.mod.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BrowserExitPortalBlock extends Block {
    public BrowserExitPortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        tryEscape(level, pos, entity);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        tryEscape(level, pos, entity);
    }

    private static void tryEscape(Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel
                && entity instanceof ServerPlayer player && !player.isOnPortalCooldown()) {
            player.setPortalCooldown(100);
            level.playSound(null, pos, ModSounds.XP_STARTUP.get(), SoundSource.AMBIENT, 1.3F, 1.2F);
            java.util.UUID pid = player.getUUID();
            UnderWorldEventDirector.schedule(1, () -> {
                ServerPlayer p = serverLevel.getServer().getPlayerList().getPlayer(pid);
                if (p != null && p.isAlive() && p.level() == serverLevel) {
                    if (ModDimensions.returnToOverworldFromNewDim(p, "message.under_world.browser_escaped")) {
                        ItemStack computer = new ItemStack(ModItems.COMPUTER.get());
                        if (!p.getInventory().add(computer)) {
                            p.drop(computer, false);
                        }
                        p.displayClientMessage(Component.translatable("message.under_world.browser_reward_computer"), false);
                    }
                }
            });
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(2) == 0) {
            level.addParticle(
                    ParticleTypes.END_ROD,
                    pos.getX() + random.nextDouble(),
                    pos.getY() + random.nextDouble(),
                    pos.getZ() + random.nextDouble(),
                    0.0D, 0.05D, 0.0D
            );
        }
    }
}
