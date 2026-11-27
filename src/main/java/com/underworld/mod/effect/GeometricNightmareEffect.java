package com.underworld.mod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class GeometricNightmareEffect extends MobEffect {
    public GeometricNightmareEffect() {
        super(MobEffectCategory.HARMFUL, 0x101018);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Purely psychological / visual geometric distortion handled client-side
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
