package com.underworld.mod.block;

import com.underworld.mod.registry.ModDimensions;
import com.underworld.mod.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

public class ComputerBlock extends HorizontalDirectionalBlock {
    public ComputerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer && !serverPlayer.isOnPortalCooldown()) {
            serverPlayer.setPortalCooldown(80);
            level.playSound(null, pos, ModSounds.XP_STARTUP.get(), SoundSource.BLOCKS, 1.3F, 1.0F);
            if (ModDimensions.isXp(level) || ModDimensions.isBrowser(level)) {
                ModDimensions.returnToOverworldFromNewDim(serverPlayer, "message.under_world.xp_escape");
            } else {
                ModDimensions.sendToXp(serverPlayer);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
