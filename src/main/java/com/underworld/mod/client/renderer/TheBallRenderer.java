package com.underworld.mod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.entity.TheBallEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class TheBallRenderer extends EntityRenderer<TheBallEntity> {
    private static final ResourceLocation TEXTURE =
            UnderWorldMod.id("textures/entity/the_ball/the_ball.png");
    private static final int STACKS = 18;
    private static final int SLICES = 28;

    public TheBallRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.55F;
    }

    @Override
    public ResourceLocation getTextureLocation(TheBallEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(TheBallEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, entity.getBbHeight() * 0.5D, 0.0D);

        float sx = entity.getMorphScaleX(partialTick) * 0.65F;
        float sy = entity.getMorphScaleY(partialTick) * 0.65F;
        float sz = entity.getMorphScaleZ(partialTick) * 0.65F;
        poseStack.scale(sx, sy, sz);

        VertexConsumer vc = bufferSource.getBuffer(RenderType.entitySolid(TEXTURE));
        PoseStack.Pose last = poseStack.last();
        Matrix4f pose = last.pose();
        Matrix3f norm = last.normal();

        float t = (entity.tickCount + partialTick) * 0.14F;

        for (int i = 0; i < STACKS; i++) {
            float lat0 = (float) Math.PI * (-0.5F + (float) i / STACKS);
            float lat1 = (float) Math.PI * (-0.5F + (float) (i + 1) / STACKS);
            float y0 = Mth.sin(lat0);
            float r0 = Mth.cos(lat0);
            float y1 = Mth.sin(lat1);
            float r1 = Mth.cos(lat1);
            float v0 = (float) i / STACKS;
            float v1 = (float) (i + 1) / STACKS;

            for (int j = 0; j < SLICES; j++) {
                float lon0 = (float) (2.0D * Math.PI * (double) j / SLICES);
                float lon1 = (float) (2.0D * Math.PI * (double) (j + 1) / SLICES);
                float x00 = Mth.cos(lon0) * r0;
                float z00 = Mth.sin(lon0) * r0;
                float x01 = Mth.cos(lon1) * r0;
                float z01 = Mth.sin(lon1) * r0;
                float x10 = Mth.cos(lon0) * r1;
                float z10 = Mth.sin(lon0) * r1;
                float x11 = Mth.cos(lon1) * r1;
                float z11 = Mth.sin(lon1) * r1;

                // Subtle organic surface ripple across the pitch-black void sphere
                float w00 = 1.0F + 0.05F * Mth.sin(t + lon0 * 3.0F + lat0 * 4.0F);
                float w01 = 1.0F + 0.05F * Mth.sin(t + lon1 * 3.0F + lat0 * 4.0F);
                float w10 = 1.0F + 0.05F * Mth.sin(t + lon0 * 3.0F + lat1 * 4.0F);
                float w11 = 1.0F + 0.05F * Mth.sin(t + lon1 * 3.0F + lat1 * 4.0F);

                float u0 = (float) j / SLICES;
                float u1 = (float) (j + 1) / SLICES;

                vc.vertex(pose, x00 * w00, y0 * w00, z00 * w00).color(10, 10, 14, 255).uv(u0, v0)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(norm, x00, y0, z00).endVertex();
                vc.vertex(pose, x10 * w10, y1 * w10, z10 * w10).color(10, 10, 14, 255).uv(u0, v1)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(norm, x10, y1, z10).endVertex();
                vc.vertex(pose, x11 * w11, y1 * w11, z11 * w11).color(10, 10, 14, 255).uv(u1, v1)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(norm, x11, y1, z11).endVertex();
                vc.vertex(pose, x01 * w01, y0 * w01, z01 * w01).color(10, 10, 14, 255).uv(u1, v0)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(norm, x01, y0, z01).endVertex();
            }
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
