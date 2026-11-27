package com.underworld.mod.client.renderer;

import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.entity.WindTurbineStalkerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class WindTurbineStalkerRenderer extends GeoEntityRenderer<WindTurbineStalkerEntity> {
    public WindTurbineStalkerRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<WindTurbineStalkerEntity>(UnderWorldMod.id("wind_turbine_stalker"), true)
                .withAltTexture(UnderWorldMod.id("wind_turbine_stalker/wind_turbine_stalker")));
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
        this.shadowRadius = 1.3F;
    }
}
