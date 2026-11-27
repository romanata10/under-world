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

    public static final RegistryObject<Item> THE_TRUTH_SPAWN_EGG = ITEMS.register("the_truth_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.THE_TRUTH, 0x050507, 0xE6E6EE, new Item.Properties()));

    public static final RegistryObject<Item> CLOCK_SPAWN_EGG = ITEMS.register("clock_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.CLOCK, 0x0D0D10, 0xECECF2, new Item.Properties()));

    public static final RegistryObject<Item> THE_MIMIC_SPAWN_EGG = ITEMS.register("the_mimic_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.THE_MIMIC, 0x050507, 0xFFAABB, new Item.Properties()));

    public static final RegistryObject<Item> DEMON_FISH_SPAWN_EGG = ITEMS.register("demon_fish_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.DEMON_FISH, 0x07090F, 0xE88FA6, new Item.Properties()));

    public static final RegistryObject<Item> THE_BALL_SPAWN_EGG = ITEMS.register("the_ball_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.THE_BALL, 0x020203, 0x20202A, new Item.Properties()));

    public static final RegistryObject<Item> LIVING_PYLON_SPAWN_EGG = ITEMS.register("living_pylon_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.LIVING_PYLON, 0x5C2E26, 0xFF3E26, new Item.Properties()));

    public static final RegistryObject<Item> WIND_TURBINE_STALKER_SPAWN_EGG = ITEMS.register("wind_turbine_stalker_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.WIND_TURBINE_STALKER, 0xE6EAEF, 0xD61C2B, new Item.Properties()));

    public static final RegistryObject<Item> THE_DOT_COM_SPAWN_EGG = ITEMS.register("the_dot_com_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.THE_DOT_COM, 0x3CE04A, 0xDE121C, new Item.Properties()));

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

    // The Error, The Time & Glitch block items
    public static final RegistryObject<Item> ERROR_GROUND = blockItem("error_ground", ModBlocks.ERROR_GROUND);
    public static final RegistryObject<Item> ERROR_SOIL = blockItem("error_soil", ModBlocks.ERROR_SOIL);
    public static final RegistryObject<Item> GLITCH_BLOCK = blockItem("glitch_block", ModBlocks.GLITCH_BLOCK);
    public static final RegistryObject<Item> CLOCK_FACE_BLOCK = blockItem("clock_face_block", ModBlocks.CLOCK_FACE_BLOCK);
    public static final RegistryObject<Item> CLOCK_RIM_BLOCK = blockItem("clock_rim_block", ModBlocks.CLOCK_RIM_BLOCK);
    public static final RegistryObject<Item> TIME_PORTAL = blockItem("time_portal", ModBlocks.TIME_PORTAL);

    // v2.4.0 block items & Computer reward
    public static final RegistryObject<Item> PLAINS_GRASS_BLOCK = blockItem("plains_grass_block", ModBlocks.PLAINS_GRASS_BLOCK);
    public static final RegistryObject<Item> PLAINS_PATH_BLOCK = blockItem("plains_path_block", ModBlocks.PLAINS_PATH_BLOCK);
    public static final RegistryObject<Item> PLAINS_PORTAL_GRASS = blockItem("plains_portal_grass", ModBlocks.PLAINS_PORTAL_GRASS);
    public static final RegistryObject<Item> PYLON_STEEL = blockItem("pylon_steel", ModBlocks.PYLON_STEEL);
    public static final RegistryObject<Item> WIND_TURBINE_PORTAL = blockItem("wind_turbine_portal", ModBlocks.WIND_TURBINE_PORTAL);
    public static final RegistryObject<Item> TURBINE_PILLAR = blockItem("turbine_pillar", ModBlocks.TURBINE_PILLAR);
    public static final RegistryObject<Item> TURBINE_BLADE = blockItem("turbine_blade", ModBlocks.TURBINE_BLADE);
    public static final RegistryObject<Item> OIL_RIG_STEEL = blockItem("oil_rig_steel", ModBlocks.OIL_RIG_STEEL);
    public static final RegistryObject<Item> OIL_RIG_PORTAL = blockItem("oil_rig_portal", ModBlocks.OIL_RIG_PORTAL);
    public static final RegistryObject<Item> XP_GRASS_BLOCK = blockItem("xp_grass_block", ModBlocks.XP_GRASS_BLOCK);
    public static final RegistryObject<Item> XP_ICON_BLOCK = blockItem("xp_icon_block", ModBlocks.XP_ICON_BLOCK);
    public static final RegistryObject<Item> XP_ICON_FALLEN = blockItem("xp_icon_fallen", ModBlocks.XP_ICON_FALLEN);
    public static final RegistryObject<Item> CHROME_PORTAL = blockItem("chrome_portal", ModBlocks.CHROME_PORTAL);
    public static final RegistryObject<Item> BROWSER_TILE = blockItem("browser_tile", ModBlocks.BROWSER_TILE);
    public static final RegistryObject<Item> BROWSER_BAR_BLOCK = blockItem("browser_bar_block", ModBlocks.BROWSER_BAR_BLOCK);
    public static final RegistryObject<Item> SITE_YOUTUBE_BLOCK = blockItem("site_youtube_block", ModBlocks.SITE_YOUTUBE_BLOCK);
    public static final RegistryObject<Item> SITE_ROBLOX_BLOCK = blockItem("site_roblox_block", ModBlocks.SITE_ROBLOX_BLOCK);
    public static final RegistryObject<Item> SITE_STEAM_BLOCK = blockItem("site_steam_block", ModBlocks.SITE_STEAM_BLOCK);
    public static final RegistryObject<Item> SITE_REDDIT_BLOCK = blockItem("site_reddit_block", ModBlocks.SITE_REDDIT_BLOCK);
    public static final RegistryObject<Item> SITE_GLITCH_BLOCK = blockItem("site_glitch_block", ModBlocks.SITE_GLITCH_BLOCK);
    public static final RegistryObject<Item> DOWNLOAD_BUTTON_BLOCK = blockItem("download_button_block", ModBlocks.DOWNLOAD_BUTTON_BLOCK);
    public static final RegistryObject<Item> BROWSER_EXIT_PORTAL = blockItem("browser_exit_portal", ModBlocks.BROWSER_EXIT_PORTAL);
    public static final RegistryObject<Item> COMPUTER = ITEMS.register("computer",
            () -> new BlockItem(ModBlocks.COMPUTER.get(), new Item.Properties().stacksTo(16)));

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
                        output.accept(THE_TRUTH_SPAWN_EGG.get());
                        output.accept(CLOCK_SPAWN_EGG.get());
                        output.accept(THE_MIMIC_SPAWN_EGG.get());
                        output.accept(DEMON_FISH_SPAWN_EGG.get());
                        output.accept(THE_BALL_SPAWN_EGG.get());
                        output.accept(LIVING_PYLON_SPAWN_EGG.get());
                        output.accept(WIND_TURBINE_STALKER_SPAWN_EGG.get());
                        output.accept(THE_DOT_COM_SPAWN_EGG.get());

                        output.accept(COMPUTER.get());
                        output.accept(PLAINS_GRASS_BLOCK.get());
                        output.accept(PLAINS_PATH_BLOCK.get());
                        output.accept(PLAINS_PORTAL_GRASS.get());
                        output.accept(PYLON_STEEL.get());
                        output.accept(WIND_TURBINE_PORTAL.get());
                        output.accept(TURBINE_PILLAR.get());
                        output.accept(TURBINE_BLADE.get());
                        output.accept(OIL_RIG_STEEL.get());
                        output.accept(OIL_RIG_PORTAL.get());
                        output.accept(XP_GRASS_BLOCK.get());
                        output.accept(XP_ICON_BLOCK.get());
                        output.accept(XP_ICON_FALLEN.get());
                        output.accept(CHROME_PORTAL.get());
                        output.accept(BROWSER_TILE.get());
                        output.accept(BROWSER_BAR_BLOCK.get());
                        output.accept(SITE_YOUTUBE_BLOCK.get());
                        output.accept(SITE_ROBLOX_BLOCK.get());
                        output.accept(SITE_STEAM_BLOCK.get());
                        output.accept(SITE_REDDIT_BLOCK.get());
                        output.accept(SITE_GLITCH_BLOCK.get());
                        output.accept(DOWNLOAD_BUTTON_BLOCK.get());
                        output.accept(BROWSER_EXIT_PORTAL.get());

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

                        output.accept(ERROR_GROUND.get());
                        output.accept(ERROR_SOIL.get());
                        output.accept(GLITCH_BLOCK.get());
                        output.accept(CLOCK_FACE_BLOCK.get());
                        output.accept(CLOCK_RIM_BLOCK.get());
                        output.accept(TIME_PORTAL.get());
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
            event.accept(THE_TRUTH_SPAWN_EGG.get());
            event.accept(CLOCK_SPAWN_EGG.get());
            event.accept(THE_MIMIC_SPAWN_EGG.get());
            event.accept(DEMON_FISH_SPAWN_EGG.get());
            event.accept(THE_BALL_SPAWN_EGG.get());
            event.accept(LIVING_PYLON_SPAWN_EGG.get());
            event.accept(WIND_TURBINE_STALKER_SPAWN_EGG.get());
            event.accept(THE_DOT_COM_SPAWN_EGG.get());
        }
    }
}
