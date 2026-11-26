package com.underworld.mod.client.renderer;

import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.entity.TreeMimicEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class TreeMimicModel extends DefaultedEntityGeoModel<TreeMimicEntity> {
    private static final ResourceLocation DISGUISED_TEXTURE =
            UnderWorldMod.id("textures/entity/tree_mimic/tree_mimic_disguised.png");
    private static final ResourceLocation AWAKE_TEXTURE =
            UnderWorldMod.id("textures/entity/tree_mimic/tree_mimic_awake.png");

    public TreeMimicModel() {
        super(UnderWorldMod.id("tree_mimic"), false);
    }

    @Override
    public ResourceLocation getTextureResource(TreeMimicEntity animatable) {
        return animatable.isAwakened() ? AWAKE_TEXTURE : DISGUISED_TEXTURE;
    }
}
