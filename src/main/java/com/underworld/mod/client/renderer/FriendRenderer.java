package com.underworld.mod.client.renderer;

import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.entity.FriendEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class FriendRenderer extends GeoEntityRenderer<FriendEntity> {
    public FriendRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<FriendEntity>(UnderWorldMod.id("friend"), true)
                .withAltTexture(UnderWorldMod.id("friend/friend")));
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
        this.shadowRadius = 0.5F;
    }
}
