package com.underworld.mod.block;

import com.underworld.mod.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class FunFlowerBlockEntity extends BlockEntity implements GeoBlockEntity {
    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("misc.idle");
    private static final RawAnimation ANIM_WATCH = RawAnimation.begin().thenLoop("misc.watch");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public FunFlowerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FUN_FLOWER.get(), pos, state);
    }

    public FunFlowerBlock.FlowerVariant getVariant() {
        if (getBlockState().getBlock() instanceof FunFlowerBlock flower) {
            return flower.getVariant();
        }
        return FunFlowerBlock.FlowerVariant.YELLOW_DAISY;
    }

    public boolean isNightActive() {
        Level lvl = this.level;
        if (lvl == null) {
            return false;
        }
        long time = lvl.getDayTime() % 24000L;
        return time >= 12500L && time <= 23500L;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "State", 6, state ->
                state.setAndContinue(isNightActive() ? ANIM_WATCH : ANIM_IDLE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
