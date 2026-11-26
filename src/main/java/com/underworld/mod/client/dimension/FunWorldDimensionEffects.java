package com.underworld.mod.client.dimension;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import com.underworld.mod.UnderWorldMod;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class FunWorldDimensionEffects extends DimensionSpecialEffects {
    private static final ResourceLocation SUN_DAY_TEXTURE =
            UnderWorldMod.id("textures/environment/fun_sun_eye.png");
    private static final ResourceLocation SUN_NIGHT_TEXTURE =
            UnderWorldMod.id("textures/environment/fun_sun_eye_night.png");

    public FunWorldDimensionEffects() {
        super(Float.NaN, true, SkyType.NORMAL, false, false);
    }

    public static float getNightFactor(ClientLevel level, float partialTick) {
        long time = level.getDayTime() % 24000L;
        float t = (float) time + partialTick;
        if (t >= 12000.0F && t < 13500.0F) {
            return (t - 12000.0F) / 1500.0F;
        }
        if (t >= 13500.0F && t <= 22800.0F) {
            return 1.0F;
        }
        if (t > 22800.0F && t < 24000.0F) {
            return 1.0F - (t - 22800.0F) / 1200.0F;
        }
        return 0.0F;
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight) {
        // Interpolate cleanly from dreamy white day horizon (#F0F8FF) to deep dark night (#050408)
        float day = Mth.clamp((sunHeight + 0.1F) * 1.4F, 0.0F, 1.0F);
        return new Vec3(
                Mth.lerp(day, 0.025D, 0.92D),
                Mth.lerp(day, 0.022D, 0.96D),
                Mth.lerp(day, 0.038D, 1.00D)
        );
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return false;
    }

    @Override
    public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack,
                                double camX, double camY, double camZ, Matrix4f projectionMatrix) {
        // Hide vanilla clouds so the saturated blue-to-white Windows 98 gradient sky stays clean
        return true;
    }

    @Override
    public boolean renderSnowAndRain(ClientLevel level, int ticks, float partialTick, LightTexture lightTexture,
                                     double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public void adjustLightmapColors(ClientLevel level, float partialTicks, float skyDarken, float blockLightRedFlicker,
                                     float skyLight, int pixelX, int pixelY, Vector3f colors) {
        float night = getNightFactor(level, partialTicks);
        if (night > 0.05F) {
            float dim = 1.0F - 0.58F * night;
            colors.mul(dim, dim * 0.95F, dim);
        }
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera,
                             Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        setupFog.run();
        float night = getNightFactor(level, partialTick);

        // Saturated Win98 blue zenith -> bright white horizon during day; black-void night
        float zR = Mth.lerp(night, 0.04F, 0.015F);
        float zG = Mth.lerp(night, 0.28F, 0.012F);
        float zB = Mth.lerp(night, 0.98F, 0.030F);

        float hR = Mth.lerp(night, 0.94F, 0.030F);
        float hG = Mth.lerp(night, 0.97F, 0.025F);
        float hB = Mth.lerp(night, 1.00F, 0.045F);

        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // Draw full sky dome with smooth vertical gradient bands
        Matrix4f skyMat = poseStack.last().pose();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        int slices = 24;
        int stacks = 10;
        float radius = 120.0F;
        for (int st = 0; st < stacks; st++) {
            float v0 = (float) st / stacks;
            float v1 = (float) (st + 1) / stacks;
            float phi0 = (v0 - 0.15F) * (Mth.PI * 0.57F);
            float phi1 = (v1 - 0.15F) * (Mth.PI * 0.57F);

            float t0 = Mth.clamp(v0 * 1.15F, 0.0F, 1.0F);
            float t1 = Mth.clamp(v1 * 1.15F, 0.0F, 1.0F);
            float r0 = Mth.lerp(t0, hR, zR), g0 = Mth.lerp(t0, hG, zG), b0 = Mth.lerp(t0, hB, zB);
            float r1 = Mth.lerp(t1, hR, zR), g1 = Mth.lerp(t1, hG, zG), b1 = Mth.lerp(t1, hB, zB);

            float y0 = Mth.sin(phi0) * radius;
            float ring0 = Mth.cos(phi0) * radius;
            float y1 = Mth.sin(phi1) * radius;
            float ring1 = Mth.cos(phi1) * radius;

            for (int sl = 0; sl < slices; sl++) {
                float a0 = (float) sl / slices * Mth.TWO_PI;
                float a1 = (float) (sl + 1) / slices * Mth.TWO_PI;

                float x00 = Mth.cos(a0) * ring0, z00 = Mth.sin(a0) * ring0;
                float x10 = Mth.cos(a1) * ring0, z10 = Mth.sin(a1) * ring0;
                float x01 = Mth.cos(a0) * ring1, z01 = Mth.sin(a0) * ring1;
                float x11 = Mth.cos(a1) * ring1, z11 = Mth.sin(a1) * ring1;

                builder.vertex(skyMat, x00, y0, z00).color(r0, g0, b0, 1.0F).endVertex();
                builder.vertex(skyMat, x10, y0, z10).color(r0, g0, b0, 1.0F).endVertex();
                builder.vertex(skyMat, x11, y1, z11).color(r1, g1, b1, 1.0F).endVertex();
                builder.vertex(skyMat, x01, y1, z01).color(r1, g1, b1, 1.0F).endVertex();
            }
        }
        BufferUploader.drawWithShader(builder.end());

        // Giant yellow Sun with one big staring eye watching over The Fun World!
        poseStack.pushPose();
        float timeOfDay = level.getTimeOfDay(partialTick);
        float sunElevation = 36.0F + 14.0F * Mth.sin(timeOfDay * Mth.TWO_PI);
        poseStack.mulPose(Axis.YP.rotationDegrees(-35.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(-sunElevation));
        Matrix4f sunMat = poseStack.last().pose();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, night > 0.5F ? SUN_NIGHT_TEXTURE : SUN_DAY_TEXTURE);

        float sunSize = 36.0F;
        float dist = -100.0F;
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(sunMat, -sunSize, -sunSize, dist).uv(0.0F, 1.0F).endVertex();
        builder.vertex(sunMat, sunSize, -sunSize, dist).uv(1.0F, 1.0F).endVertex();
        builder.vertex(sunMat, sunSize, sunSize, dist).uv(1.0F, 0.0F).endVertex();
        builder.vertex(sunMat, -sunSize, sunSize, dist).uv(0.0F, 0.0F).endVertex();
        BufferUploader.drawWithShader(builder.end());
        poseStack.popPose();

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        return true;
    }
}
