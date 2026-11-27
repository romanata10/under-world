package com.underworld.mod.client.renderer;

import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.entity.DemonFishEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class DemonFishRenderer extends GeoEntityRenderer<DemonFishEntity> {
    public DemonFishRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<DemonFishEntity>(UnderWorldMod.id("demon_fish"), true)
                .withAltTexture(UnderWorldMod.id("demon_fish/demon_fish")));
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
        this.shadowRadius = 0.70F;
    }
}
