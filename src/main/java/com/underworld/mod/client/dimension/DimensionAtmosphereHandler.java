package com.underworld.mod.client.dimension;

import com.underworld.mod.registry.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class DimensionAtmosphereHandler {
    private DimensionAtmosphereHandler() {}

    @SubscribeEvent
    public static void onComputeFogColor(final ViewportEvent.ComputeFogColor event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        if (ModDimensions.isFunWorld(level)) {
            float night = FunWorldDimensionEffects.getNightFactor(level, (float) event.getPartialTick());
            event.setRed(Mth.lerp(night, 0.92F, 0.020F));
            event.setGreen(Mth.lerp(night, 0.96F, 0.018F));
            event.setBlue(Mth.lerp(night, 1.00F, 0.032F));
        } else if (ModDimensions.isMaze(level)) {
            event.setRed(0.015F);
            event.setGreen(0.012F);
            event.setBlue(0.020F);
        }
    }

    @SubscribeEvent
    public static void onRenderFog(final ViewportEvent.RenderFog event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        if (ModDimensions.isMaze(level)) {
            event.setNearPlaneDistance(2.0F);
            event.setFarPlaneDistance(24.0F);
            event.setCanceled(true);
        } else if (ModDimensions.isFunWorld(level)) {
            float night = FunWorldDimensionEffects.getNightFactor(level, (float) event.getPartialTick());
            if (night > 0.2F) {
                event.setNearPlaneDistance(Mth.lerp(night, 20.0F, 4.0F));
                event.setFarPlaneDistance(Mth.lerp(night, 128.0F, 36.0F));
                event.setCanceled(true);
            }
        }
    }
}
