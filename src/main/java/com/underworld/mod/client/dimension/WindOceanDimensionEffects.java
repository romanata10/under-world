package com.underworld.mod.client.dimension;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

public class WindOceanDimensionEffects extends DimensionSpecialEffects {
    public WindOceanDimensionEffects() {
        // Misty overcast pale sky matching offshore turbine ocean horizon (image-16)
        super(155.0F, true, SkyType.NORMAL, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight) {
        return new Vec3(0.73D, 0.77D, 0.81D);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return true;
    }
}
