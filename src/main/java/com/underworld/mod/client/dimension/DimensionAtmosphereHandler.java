package com.underworld.mod.client.dimension;

import com.underworld.mod.registry.ModDimensions;
import com.underworld.mod.world.dimension.BrowserChunkGenerator;
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
        } else if (ModDimensions.isError(level)) {
            event.setRed(0.75F);
            event.setGreen(0.85F);
            event.setBlue(1.00F);
        } else if (ModDimensions.isTime(level)) {
            event.setRed(0.67F);
            event.setGreen(0.77F);
            event.setBlue(1.00F);
        } else if (ModDimensions.isPlains(level)) {
            float night = level.isNight() ? 1.0F : 0.0F;
            event.setRed(Mth.lerp(night, 0.76F, 0.02F));
            event.setGreen(Mth.lerp(night, 0.80F, 0.02F));
            event.setBlue(Mth.lerp(night, 0.83F, 0.035F));
        } else if (ModDimensions.isWindOcean(level)) {
            event.setRed(0.73F);
            event.setGreen(0.77F);
            event.setBlue(0.81F);
        } else if (ModDimensions.isXp(level)) {
            event.setRed(0.56F);
            event.setGreen(0.78F);
            event.setBlue(0.98F);
        } else if (ModDimensions.isBrowser(level)) {
            var player = Minecraft.getInstance().player;
            boolean corrupt = player != null && "minecraft_free_download".equals(
                    BrowserChunkGenerator.getSiteId(player.getBlockX(), player.getBlockZ()));
            if (corrupt) {
                event.setRed(0.02F);
                event.setGreen(0.02F);
                event.setBlue(0.03F);
            } else {
                event.setRed(0.94F);
                event.setGreen(0.95F);
                event.setBlue(0.98F);
            }
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
        } else if (ModDimensions.isTime(level)) {
            event.setNearPlaneDistance(48.0F);
            event.setFarPlaneDistance(160.0F);
            event.setCanceled(true);
        } else if (ModDimensions.isFunWorld(level)) {
            float night = FunWorldDimensionEffects.getNightFactor(level, (float) event.getPartialTick());
            if (night > 0.2F) {
                event.setNearPlaneDistance(Mth.lerp(night, 20.0F, 4.0F));
                event.setFarPlaneDistance(Mth.lerp(night, 128.0F, 36.0F));
                event.setCanceled(true);
            }
        } else if (ModDimensions.isPlains(level)) {
            if (level.isNight()) {
                event.setNearPlaneDistance(4.0F);
                event.setFarPlaneDistance(46.0F);
            } else {
                event.setNearPlaneDistance(32.0F);
                event.setFarPlaneDistance(112.0F);
            }
            event.setCanceled(true);
        } else if (ModDimensions.isWindOcean(level)) {
            event.setNearPlaneDistance(16.0F);
            event.setFarPlaneDistance(72.0F);
            event.setCanceled(true);
        } else if (ModDimensions.isBrowser(level)) {
            var player = Minecraft.getInstance().player;
            if (player != null && "minecraft_free_download".equals(
                    BrowserChunkGenerator.getSiteId(player.getBlockX(), player.getBlockZ()))) {
                event.setNearPlaneDistance(4.0F);
                event.setFarPlaneDistance(36.0F);
                event.setCanceled(true);
            }
        }
    }
}
