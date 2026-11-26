package com.underworld.mod.client.model;

import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.entity.TheTruthEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class TheTruthModel extends DefaultedEntityGeoModel<TheTruthEntity> {
    public TheTruthModel() {
        super(UnderWorldMod.id("the_truth"));
    }
}
