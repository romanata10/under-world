package com.underworld.mod.client.dimension;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class PlainsDimensionEffects extends DimensionSpecialEffects {
    public PlainsDimensionEffects() {
        // Pale hazy plains overcast sky with normal day/night cycle
        super(180.0F, true, SkyType.NORMAL, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight) {
        float day = Mth.clamp((sunHeight + 0.15F) * 1.4F, 0.0F, 1.0F);
        return new Vec3(
                Mth.lerp(day, 0.015D, 0.76D),
                Mth.lerp(day, 0.015D, 0.80D),
                Mth.lerp(day, 0.025D, 0.83D)
        );
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return false;
    }

    @Override
    public void adjustLightmapColors(ClientLevel level, float partialTicks, float skyDarken, float blockLightRedFlicker,
                                     float skyLight, int pixelX, int pixelY, Vector3f colors) {
        if (level.isNight()) {
            colors.mul(0.55F, 0.55F, 0.62F);
        }
    }
}
