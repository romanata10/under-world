package com.underworld.mod.client.renderer;

import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.entity.HandsEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class HandsRenderer extends GeoEntityRenderer<HandsEntity> {
    public HandsRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<HandsEntity>(UnderWorldMod.id("hands"), true)
                .withAltTexture(UnderWorldMod.id("hands/hands")));
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
        this.shadowRadius = 0.85F;
    }
}
