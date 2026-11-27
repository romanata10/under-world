package com.underworld.mod.registry;

import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.entity.ClockEntity;
import com.underworld.mod.entity.DemonFishEntity;
import com.underworld.mod.entity.EyesOfFunEntity;
import com.underworld.mod.entity.FriendEntity;
import com.underworld.mod.entity.HandsEntity;
import com.underworld.mod.entity.LivingPylonEntity;
import com.underworld.mod.entity.SmileEntity;
import com.underworld.mod.entity.TheBallEntity;
import com.underworld.mod.entity.TheDotComEntity;
import com.underworld.mod.entity.TheMimicEntity;
import com.underworld.mod.entity.TheSkyEntity;
import com.underworld.mod.entity.TheTruthEntity;
import com.underworld.mod.entity.TreeMimicEntity;
import com.underworld.mod.entity.WindTurbineStalkerEntity;
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

    public static final RegistryObject<EntityType<ClockEntity>> CLOCK = ENTITIES.register("clock",
            () -> EntityType.Builder.of(ClockEntity::new, MobCategory.MONSTER)
                    .sized(0.85F, 3.25F)
                    .clientTrackingRange(20)
                    .updateInterval(1)
                    .build(UnderWorldMod.id("clock").toString()));

    public static final RegistryObject<EntityType<TheMimicEntity>> THE_MIMIC = ENTITIES.register("the_mimic",
            () -> EntityType.Builder.of(TheMimicEntity::new, MobCategory.MONSTER)
                    .sized(0.75F, 2.25F)
                    .clientTrackingRange(18)
                    .updateInterval(1)
                    .build(UnderWorldMod.id("the_mimic").toString()));

    public static final RegistryObject<EntityType<DemonFishEntity>> DEMON_FISH = ENTITIES.register("demon_fish",
            () -> EntityType.Builder.of(DemonFishEntity::new, MobCategory.WATER_CREATURE)
                    .sized(1.15F, 0.85F)
                    .clientTrackingRange(18)
                    .updateInterval(1)
                    .build(UnderWorldMod.id("demon_fish").toString()));

    public static final RegistryObject<EntityType<TheBallEntity>> THE_BALL = ENTITIES.register("the_ball",
            () -> EntityType.Builder.of(TheBallEntity::new, MobCategory.AMBIENT)
                    .sized(1.35F, 1.35F)
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .build(UnderWorldMod.id("the_ball").toString()));

    public static final RegistryObject<EntityType<LivingPylonEntity>> LIVING_PYLON = ENTITIES.register("living_pylon",
            () -> EntityType.Builder.of(LivingPylonEntity::new, MobCategory.MONSTER)
                    .sized(1.85F, 8.6F)
                    .clientTrackingRange(24)
                    .updateInterval(1)
                    .build(UnderWorldMod.id("living_pylon").toString()));

    public static final RegistryObject<EntityType<WindTurbineStalkerEntity>> WIND_TURBINE_STALKER = ENTITIES.register("wind_turbine_stalker",
            () -> EntityType.Builder.of(WindTurbineStalkerEntity::new, MobCategory.MONSTER)
                    .sized(1.6F, 10.5F)
                    .clientTrackingRange(24)
                    .updateInterval(1)
                    .build(UnderWorldMod.id("wind_turbine_stalker").toString()));

    public static final RegistryObject<EntityType<TheDotComEntity>> THE_DOT_COM = ENTITIES.register("the_dot_com",
            () -> EntityType.Builder.of(TheDotComEntity::new, MobCategory.MONSTER)
                    .sized(1.15F, 4.9F)
                    .clientTrackingRange(24)
                    .updateInterval(1)
                    .build(UnderWorldMod.id("the_dot_com").toString()));

    private ModEntities() {}

    public static void registerAttributes(final EntityAttributeCreationEvent event) {
        event.put(FRIEND.get(), FriendEntity.createAttributes().build());
        event.put(HANDS.get(), HandsEntity.createAttributes().build());
        event.put(TREE_MIMIC.get(), TreeMimicEntity.createAttributes().build());
        event.put(THE_SKY.get(), TheSkyEntity.createAttributes().build());
        event.put(EYES_OF_FUN.get(), EyesOfFunEntity.createAttributes().build());
        event.put(SMILE.get(), SmileEntity.createAttributes().build());
        event.put(THE_TRUTH.get(), TheTruthEntity.createAttributes().build());
        event.put(CLOCK.get(), ClockEntity.createAttributes().build());
        event.put(THE_MIMIC.get(), TheMimicEntity.createAttributes().build());
        event.put(DEMON_FISH.get(), DemonFishEntity.createAttributes().build());
        event.put(THE_BALL.get(), TheBallEntity.createAttributes().build());
        event.put(LIVING_PYLON.get(), LivingPylonEntity.createAttributes().build());
        event.put(WIND_TURBINE_STALKER.get(), WindTurbineStalkerEntity.createAttributes().build());
        event.put(THE_DOT_COM.get(), TheDotComEntity.createAttributes().build());
    }
}
