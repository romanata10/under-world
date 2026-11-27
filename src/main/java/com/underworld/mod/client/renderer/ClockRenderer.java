package com.underworld.mod.client.renderer;

import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.entity.ClockEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class ClockRenderer extends GeoEntityRenderer<ClockEntity> {
    public ClockRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<ClockEntity>(UnderWorldMod.id("clock"), true)
                .withAltTexture(UnderWorldMod.id("clock/clock")));
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
        this.shadowRadius = 0.65F;
    }
}
