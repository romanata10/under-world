package com.underworld.mod.registry;

import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.entity.EyesOfFunEntity;
import com.underworld.mod.entity.FriendEntity;
import com.underworld.mod.entity.HandsEntity;
import com.underworld.mod.entity.SmileEntity;
import com.underworld.mod.entity.TheSkyEntity;
import com.underworld.mod.entity.TheTruthEntity;
import com.underworld.mod.entity.TreeMimicEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, UnderWorldMod.MOD_ID);

    public static final RegistryObject<EntityType<FriendEntity>> FRIEND = ENTITIES.register("friend",
            () -> EntityType.Builder.of(FriendEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(12)
                    .build(UnderWorldMod.id("friend").toString()));

    public static final RegistryObject<EntityType<HandsEntity>> HANDS = ENTITIES.register("hands",
            () -> EntityType.Builder.of(HandsEntity::new, MobCategory.MONSTER)
                    .sized(1.3F, 2.7F)
                    .clientTrackingRange(16)
                    .build(UnderWorldMod.id("hands").toString()));

    public static final RegistryObject<EntityType<TreeMimicEntity>> TREE_MIMIC = ENTITIES.register("tree_mimic",
            () -> EntityType.Builder.of(TreeMimicEntity::new, MobCategory.MONSTER)
                    .sized(1.2F, 4.5F)
                    .clientTrackingRange(12)
                    .build(UnderWorldMod.id("tree_mimic").toString()));

    public static final RegistryObject<EntityType<TheSkyEntity>> THE_SKY = ENTITIES.register("the_sky",
            () -> EntityType.Builder.of(TheSkyEntity::new, MobCategory.AMBIENT)
                    .sized(8.0F, 90.0F)
                    .clientTrackingRange(24)
                    .build(UnderWorldMod.id("the_sky").toString()));

    public static final RegistryObject<EntityType<EyesOfFunEntity>> EYES_OF_FUN = ENTITIES.register("eyes_of_fun",
            () -> EntityType.Builder.of(EyesOfFunEntity::new, MobCategory.MONSTER)
                    .sized(0.95F, 2.85F)
                    .clientTrackingRange(16)
                    .build(UnderWorldMod.id("eyes_of_fun").toString()));

    public static final RegistryObject<EntityType<SmileEntity>> SMILE = ENTITIES.register("smile",
            () -> EntityType.Builder.of(SmileEntity::new, MobCategory.MONSTER)
                    .sized(1.15F, 2.60F)
                    .clientTrackingRange(18)
                    .build(UnderWorldMod.id("smile").toString()));

    public static final RegistryObject<EntityType<TheTruthEntity>> THE_TRUTH = ENTITIES.register("the_truth",
            () -> EntityType.Builder.of(TheTruthEntity::new, MobCategory.MONSTER)
                    .sized(10.0F, 48.0F)
                    .clientTrackingRange(24)
                    .updateInterval(1)
                    .build(UnderWorldMod.id("the_truth").toString()));

    private ModEntities() {}

    public static void registerAttributes(final EntityAttributeCreationEvent event) {
        event.put(FRIEND.get(), FriendEntity.createAttributes().build());
        event.put(HANDS.get(), HandsEntity.createAttributes().build());
        event.put(TREE_MIMIC.get(), TreeMimicEntity.createAttributes().build());
        event.put(THE_SKY.get(), TheSkyEntity.createAttributes().build());
        event.put(EYES_OF_FUN.get(), EyesOfFunEntity.createAttributes().build());
        event.put(SMILE.get(), SmileEntity.createAttributes().build());
        event.put(THE_TRUTH.get(), TheTruthEntity.createAttributes().build());
    }
}
