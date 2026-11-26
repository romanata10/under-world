package com.underworld.mod.registry;

import com.underworld.mod.UnderWorldMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
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

    // Spawn eggs
    public static final RegistryObject<Item> FRIEND_SPAWN_EGG = ITEMS.register("friend_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.FRIEND, 0x101015, 0x8F4B9E, new Item.Properties()));

    public static final RegistryObject<Item> HANDS_SPAWN_EGG = ITEMS.register("hands_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.HANDS, 0x09090D, 0xE89C8A, new Item.Properties()));

    public static final RegistryObject<Item> TREE_MIMIC_SPAWN_EGG = ITEMS.register("tree_mimic_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.TREE_MIMIC, 0x4A3622, 0xECEFF5, new Item.Properties()));

    public static final RegistryObject<Item> THE_SKY_SPAWN_EGG = ITEMS.register("the_sky_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.THE_SKY, 0x050507, 0x262634, new Item.Properties()));

    public static final RegistryObject<Item> EYES_OF_FUN_SPAWN_EGG = ITEMS.register("eyes_of_fun_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.EYES_OF_FUN, 0x8EFF00, 0xFF3B00, new Item.Properties()));

    public static final RegistryObject<Item> SMILE_SPAWN_EGG = ITEMS.register("smile_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.SMILE, 0x07070A, 0xFFF058, new Item.Properties()));

    // Fun World & Maze block items
    public static final RegistryObject<Item> FUN_GRASS_BLOCK = blockItem("fun_grass_block", ModBlocks.FUN_GRASS_BLOCK);
    public static final RegistryObject<Item> FUN_DIRT = blockItem("fun_dirt", ModBlocks.FUN_DIRT);
    public static final RegistryObject<Item> FUN_GRASS_TUFT = blockItem("fun_grass_tuft", ModBlocks.FUN_GRASS_TUFT);
    public static final RegistryObject<Item> FUN_MUSHROOM_BLUE = blockItem("fun_mushroom_blue", ModBlocks.FUN_MUSHROOM_BLUE);
    public static final RegistryObject<Item> FUN_MUSHROOM_RED = blockItem("fun_mushroom_red", ModBlocks.FUN_MUSHROOM_RED);
    public static final RegistryObject<Item> FUN_YELLOW_FLOWER = blockItem("fun_yellow_flower", ModBlocks.FUN_YELLOW_FLOWER);
    public static final RegistryObject<Item> FUN_BLUE_FLOWER = blockItem("fun_blue_flower", ModBlocks.FUN_BLUE_FLOWER);
    public static final RegistryObject<Item> FUN_FLAT_RED_FLOWER = blockItem("fun_flat_red_flower", ModBlocks.FUN_FLAT_RED_FLOWER);
    public static final RegistryObject<Item> FUN_FLAT_BLUE_FLOWER = blockItem("fun_flat_blue_flower", ModBlocks.FUN_FLAT_BLUE_FLOWER);
    public static final RegistryObject<Item> FUN_FLAT_WHITE_FLOWER = blockItem("fun_flat_white_flower", ModBlocks.FUN_FLAT_WHITE_FLOWER);
    public static final RegistryObject<Item> MAZE_BRICK = blockItem("maze_brick", ModBlocks.MAZE_BRICK);
    public static final RegistryObject<Item> MAZE_FLOOR = blockItem("maze_floor", ModBlocks.MAZE_FLOOR);
    public static final RegistryObject<Item> MAZE_CEILING = blockItem("maze_ceiling", ModBlocks.MAZE_CEILING);
    public static final RegistryObject<Item> MAZE_PORTAL = blockItem("maze_portal", ModBlocks.MAZE_PORTAL);
    public static final RegistryObject<Item> MAZE_TELEPORTER = blockItem("maze_teleporter", ModBlocks.MAZE_TELEPORTER);

    public static final RegistryObject<CreativeModeTab> UNDER_WORLD_TAB = CREATIVE_TABS.register("under_world",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.under_world"))
                    .icon(() -> new ItemStack(EYES_OF_FUN_SPAWN_EGG.get()))
                    .displayItems((params, output) -> {
                        output.accept(FRIEND_SPAWN_EGG.get());
                        output.accept(HANDS_SPAWN_EGG.get());
                        output.accept(TREE_MIMIC_SPAWN_EGG.get());
                        output.accept(THE_SKY_SPAWN_EGG.get());
                        output.accept(EYES_OF_FUN_SPAWN_EGG.get());
                        output.accept(SMILE_SPAWN_EGG.get());

                        output.accept(FUN_GRASS_BLOCK.get());
                        output.accept(FUN_DIRT.get());
                        output.accept(FUN_GRASS_TUFT.get());
                        output.accept(FUN_YELLOW_FLOWER.get());
                        output.accept(FUN_BLUE_FLOWER.get());
                        output.accept(FUN_FLAT_RED_FLOWER.get());
                        output.accept(FUN_FLAT_BLUE_FLOWER.get());
                        output.accept(FUN_FLAT_WHITE_FLOWER.get());
                        output.accept(FUN_MUSHROOM_BLUE.get());
                        output.accept(FUN_MUSHROOM_RED.get());

                        output.accept(MAZE_BRICK.get());
                        output.accept(MAZE_FLOOR.get());
                        output.accept(MAZE_CEILING.get());
                        output.accept(MAZE_PORTAL.get());
                        output.accept(MAZE_TELEPORTER.get());
                    })
                    .build());

    private ModItems() {}

    private static RegistryObject<Item> blockItem(String name, RegistryObject<Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void addCreativeTabItems(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.SPAWN_EGGS)) {
            event.accept(FRIEND_SPAWN_EGG.get());
            event.accept(HANDS_SPAWN_EGG.get());
            event.accept(TREE_MIMIC_SPAWN_EGG.get());
            event.accept(THE_SKY_SPAWN_EGG.get());
            event.accept(EYES_OF_FUN_SPAWN_EGG.get());
            event.accept(SMILE_SPAWN_EGG.get());
        }
    }
}
