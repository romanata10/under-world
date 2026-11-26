package com.underworld.mod.registry;

import com.underworld.mod.UnderWorldMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, UnderWorldMod.MOD_ID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, UnderWorldMod.MOD_ID);

    public static final RegistryObject<Item> FRIEND_SPAWN_EGG = ITEMS.register("friend_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.FRIEND, 0x101015, 0x8F4B9E, new Item.Properties()));

    public static final RegistryObject<Item> HANDS_SPAWN_EGG = ITEMS.register("hands_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.HANDS, 0x09090D, 0xE89C8A, new Item.Properties()));

    public static final RegistryObject<Item> TREE_MIMIC_SPAWN_EGG = ITEMS.register("tree_mimic_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.TREE_MIMIC, 0x4A3622, 0xECEFF5, new Item.Properties()));

    public static final RegistryObject<Item> THE_SKY_SPAWN_EGG = ITEMS.register("the_sky_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.THE_SKY, 0x050507, 0x262634, new Item.Properties()));

    public static final RegistryObject<CreativeModeTab> UNDER_WORLD_TAB = CREATIVE_TABS.register("under_world",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.under_world"))
                    .icon(() -> new ItemStack(FRIEND_SPAWN_EGG.get()))
                    .displayItems((params, output) -> {
                        output.accept(FRIEND_SPAWN_EGG.get());
                        output.accept(HANDS_SPAWN_EGG.get());
                        output.accept(TREE_MIMIC_SPAWN_EGG.get());
                        output.accept(THE_SKY_SPAWN_EGG.get());
                    })
                    .build());

    private ModItems() {}

    public static void addCreativeTabItems(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.SPAWN_EGGS)) {
            event.accept(FRIEND_SPAWN_EGG.get());
            event.accept(HANDS_SPAWN_EGG.get());
            event.accept(TREE_MIMIC_SPAWN_EGG.get());
            event.accept(THE_SKY_SPAWN_EGG.get());
        }
    }
}
