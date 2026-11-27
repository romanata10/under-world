package com.underworld.mod.client.renderer;

import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.entity.LivingPylonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class LivingPylonRenderer extends GeoEntityRenderer<LivingPylonEntity> {
    public LivingPylonRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<LivingPylonEntity>(UnderWorldMod.id("living_pylon"), true)
                .withAltTexture(UnderWorldMod.id("living_pylon/living_pylon")));
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
        this.shadowRadius = 1.4F;
    }
}
