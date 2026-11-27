package com.underworld.mod.client.dimension;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.underworld.mod.UnderWorldMod;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class TimeDimensionEffects extends DimensionSpecialEffects {
    private static final ResourceLocation CLOCK_PLANE_TEXTURE =
            UnderWorldMod.id("textures/environment/time_clock_plane.png");

    public TimeDimensionEffects() {
        // Clouds float naturally through the clock-filled bright sky
        super(120.0F, true, SkyType.NORMAL, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight) {
        return new Vec3(0.67D, 0.77D, 1.00D);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return false;
    }

    @Override
    public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack,
                                double camX, double camY, double camZ, Matrix4f projectionMatrix) {
        // Let Minecraft render drifting white clouds high above the void clocks
        return false;
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera,
                             Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        setupFog.run();

        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // 1. Dreamy bright blue-periwinkle endless sky sphere (top to bottom) matching image-9.png
        Matrix4f skyMat = poseStack.last().pose();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        int slices = 24;
        int stacks = 12;
        float radius = 140.0F;
        for (int st = 0; st < stacks; st++) {
            float v0 = (float) st / stacks;
            float v1 = (float) (st + 1) / stacks;
            float phi0 = (v0 - 0.5F) * Mth.PI;
            float phi1 = (v1 - 0.5F) * Mth.PI;

            float k0 = 1.0F - Math.abs(v0 - 0.5F) * 2.0F;
            float k1 = 1.0F - Math.abs(v1 - 0.5F) * 2.0F;
            // Rich sky blue above/below -> brighter dreamy horizon band
            float r0 = Mth.lerp(k0, 0.36F, 0.74F), g0 = Mth.lerp(k0, 0.54F, 0.83F), b0 = Mth.lerp(k0, 0.96F, 1.00F);
            float r1 = Mth.lerp(k1, 0.36F, 0.74F), g1 = Mth.lerp(k1, 0.54F, 0.83F), b1 = Mth.lerp(k1, 0.96F, 1.00F);

            float y0 = Mth.sin(phi0) * radius;
            float ring0 = Mth.cos(phi0) * radius;
            float y1 = Mth.sin(phi1) * radius;
            float ring1 = Mth.cos(phi1) * radius;

            for (int sl = 0; sl < slices; sl++) {
                float a0 = (float) sl / slices * Mth.TWO_PI;
                float a1 = (float) (sl + 1) / slices * Mth.TWO_PI;

                builder.vertex(skyMat, Mth.cos(a0) * ring0, y0, Mth.sin(a0) * ring0).color(r0, g0, b0, 1.0F).endVertex();
                builder.vertex(skyMat, Mth.cos(a1) * ring0, y0, Mth.sin(a1) * ring0).color(r0, g0, b0, 1.0F).endVertex();
                builder.vertex(skyMat, Mth.cos(a1) * ring1, y1, Mth.sin(a1) * ring1).color(r1, g1, b1, 1.0F).endVertex();
                builder.vertex(skyMat, Mth.cos(a0) * ring1, y1, Mth.sin(a0) * ring1).color(r1, g1, b1, 1.0F).endVertex();
            }
        }
        BufferUploader.drawWithShader(builder.end());

        // 2. Repeating perspective grids of round analog clock discs BOTH above and below the player!
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, CLOCK_PLANE_TEXTURE);

        Vec3 camPos = camera.getPosition();
        float[] planes = new float[]{48.0F, 82.0F, -36.0F, -70.0F};
        float spacing = 22.0F;
        float clockHalf = 6.2F;

        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        for (float planeY : planes) {
            double offX = Math.floor(camPos.x / spacing) * spacing - camPos.x;
            double offZ = Math.floor(camPos.z / spacing) * spacing - camPos.z;
            for (int gx = -6; gx <= 6; gx++) {
                for (int gz = -6; gz <= 6; gz++) {
                    float cx = (float) (offX + gx * spacing);
                    float cz = (float) (offZ + gz * spacing);
                    float dist = Mth.sqrt(cx * cx + cz * cz);
                    float alpha = Mth.clamp(1.0F - (dist - 65.0F) / 68.0F, 0.0F, 0.96F);
                    if (alpha <= 0.02F) {
                        continue;
                    }
                    // Subtle warm-blue atmospheric perspective tint on far planes
                    float tint = Mth.clamp(0.95F - dist / 240.0F, 0.72F, 1.0F);
                    builder.vertex(skyMat, cx - clockHalf, planeY, cz - clockHalf).uv(0.0F, 0.0F).color(tint, tint, 1.0F, alpha).endVertex();
                    builder.vertex(skyMat, cx + clockHalf, planeY, cz - clockHalf).uv(1.0F, 0.0F).color(tint, tint, 1.0F, alpha).endVertex();
                    builder.vertex(skyMat, cx + clockHalf, planeY, cz + clockHalf).uv(1.0F, 1.0F).color(tint, tint, 1.0F, alpha).endVertex();
                    builder.vertex(skyMat, cx - clockHalf, planeY, cz + clockHalf).uv(0.0F, 1.0F).color(tint, tint, 1.0F, alpha).endVertex();
                }
            }
        }
        BufferUploader.drawWithShader(builder.end());

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        return true;
    }
}
