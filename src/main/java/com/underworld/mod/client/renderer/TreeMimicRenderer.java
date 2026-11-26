package com.underworld.mod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.underworld.mod.entity.TreeMimicEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class TreeMimicRenderer extends GeoEntityRenderer<TreeMimicEntity> {
    public TreeMimicRenderer(EntityRendererProvider.Context context) {
        super(context, new TreeMimicModel());
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this) {
            @Override
            public void render(PoseStack poseStack, TreeMimicEntity animatable, BakedGeoModel bakedModel,
                               RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                               float partialTick, int packedLight, int packedOverlay) {
                if (animatable.isAwakened()) {
                    super.render(poseStack, animatable, bakedModel, renderType, bufferSource, buffer,
                            partialTick, packedLight, packedOverlay);
                }
            }
        });
        this.shadowRadius = 0.7F;
    }
}
