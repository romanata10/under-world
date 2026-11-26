package com.underworld.mod.client.renderer;

import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.entity.EyesOfFunEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class EyesOfFunRenderer extends GeoEntityRenderer<EyesOfFunEntity> {
    public EyesOfFunRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<EyesOfFunEntity>(UnderWorldMod.id("eyes_of_fun"), false)
                .withAltTexture(UnderWorldMod.id("eyes_of_fun/eyes_of_fun")));
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
        this.shadowRadius = 0.55F;
    }
}
