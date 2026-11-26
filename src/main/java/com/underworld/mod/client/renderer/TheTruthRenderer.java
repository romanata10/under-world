package com.underworld.mod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.underworld.mod.client.model.TheTruthModel;
import com.underworld.mod.entity.TheTruthEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class TheTruthRenderer extends GeoEntityRenderer<TheTruthEntity> {
    public TheTruthRenderer(EntityRendererProvider.Context context) {
        super(context, new TheTruthModel());
        this.shadowRadius = 4.5F;
        // Geo is 160 units tall x 96 units wide -> scale 5.0F = exactly 50 blocks tall x 30 blocks wide
        this.withScale(5.0F);
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public boolean shouldRender(TheTruthEntity entity, Frustum camera, double camX, double camY, double camZ) {
        if (!entity.shouldRender(camX, camY, camZ)) {
            return false;
        }
        return camera.isVisible(entity.getBoundingBoxForCulling());
    }

    @Override
    protected int getBlockLightLevel(TheTruthEntity entity, BlockPos pos) {
        return Math.max(super.getBlockLightLevel(entity, pos), 10);
    }

    @Override
    public int getPackedOverlay(TheTruthEntity animatable, float u) {
        return super.getPackedOverlay(animatable, u);
    }

    @Override
    public void preRender(PoseStack poseStack, TheTruthEntity animatable, BakedGeoModel model,
                          MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay,
                          float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, red, green, blue, alpha);
        if (!isReRender && animatable.getTruthState() == TheTruthEntity.STATE_EMERGE) {
            float progress = animatable.getEmergeProgress();
            // Ease-out rise from 50 world blocks below ground (divided by 5.0F renderer scale)
            float eased = 1.0F - (1.0F - progress) * (1.0F - progress);
            poseStack.translate(0.0D, -10.0D * (1.0D - eased), 0.0D);
        }
    }

    @Override
    public RenderType getRenderType(TheTruthEntity animatable, ResourceLocation texture,
                                    MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }

    @Override
    public void actuallyRender(PoseStack poseStack, TheTruthEntity animatable, BakedGeoModel model,
                               RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                               boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        int safeLight = LightTexture.pack(
                Math.max(8, LightTexture.block(packedLight)),
                Math.max(10, LightTexture.sky(packedLight))
        );
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender,
                partialTick, safeLight, packedOverlay, red, green, blue, alpha);
    }
}
