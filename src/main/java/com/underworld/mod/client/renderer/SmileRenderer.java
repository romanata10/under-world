package com.underworld.mod.client.renderer;

import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.entity.SmileEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class SmileRenderer extends GeoEntityRenderer<SmileEntity> {
    public SmileRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<SmileEntity>(UnderWorldMod.id("smile"), true)
                .withAltTexture(UnderWorldMod.id("smile/smile")));
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
        this.shadowRadius = 0.7F;
    }
}
