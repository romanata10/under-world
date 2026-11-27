package com.underworld.mod.client.renderer;

import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.entity.TheDotComEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class TheDotComRenderer extends GeoEntityRenderer<TheDotComEntity> {
    public TheDotComRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<TheDotComEntity>(UnderWorldMod.id("the_dot_com"), true)
                .withAltTexture(UnderWorldMod.id("the_dot_com/the_dot_com")));
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
        this.shadowRadius = 0.8F;
    }
}
