package com.underworld.mod.client.dimension;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

public class XpDimensionEffects extends DimensionSpecialEffects {
    public XpDimensionEffects() {
        // Iconic Windows XP Bliss sky & clouds (image-17)
        super(192.0F, true, SkyType.NORMAL, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight) {
        return new Vec3(0.56D, 0.78D, 0.98D);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return false;
    }
}
