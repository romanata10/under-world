package com.underworld.mod.client.dimension;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

public class BrowserDimensionEffects extends DimensionSpecialEffects {
    public BrowserDimensionEffects() {
        super(Float.NaN, true, SkyType.NORMAL, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight) {
        return new Vec3(0.94D, 0.95D, 0.98D);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return false;
    }
}
