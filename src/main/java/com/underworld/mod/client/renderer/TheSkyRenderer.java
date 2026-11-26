package com.underworld.mod.client.renderer;

import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.entity.TheSkyEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class TheSkyRenderer extends GeoEntityRenderer<TheSkyEntity> {
    public TheSkyRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<TheSkyEntity>(UnderWorldMod.id("the_sky"), false)
                .withAltTexture(UnderWorldMod.id("the_sky/the_sky")));
        this.withScale(10.0F);
        this.shadowRadius = 4.0F;
    }

    @Override
    public boolean shouldRender(TheSkyEntity entity, Frustum frustum, double camX, double camY, double camZ) {
        if (!entity.shouldRender(camX, camY, camZ)) {
            return false;
        }
        return frustum.isVisible(entity.getBoundingBoxForCulling());
    }
}
