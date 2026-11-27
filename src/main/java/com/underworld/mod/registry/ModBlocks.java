package com.underworld.mod.registry;

import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.block.FunFlowerBlock;
import com.underworld.mod.block.FunPlantBlock;
import com.underworld.mod.block.MazePortalBlock;
import com.underworld.mod.block.MazeTeleporterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, UnderWorldMod.MOD_ID);

    public static final RegistryObject<Block> FUN_GRASS_BLOCK = BLOCKS.register("fun_grass_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .strength(0.6F)
                    .sound(SoundType.GRASS)));

    public static final RegistryObject<Block> FUN_DIRT = BLOCKS.register("fun_dirt",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(0.5F)
                    .sound(SoundType.GRAVEL)));

    public static final RegistryObject<Block> FUN_GRASS_TUFT = BLOCKS.register("fun_grass_tuft",
            () -> new FunPlantBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)));

    public static final RegistryObject<Block> FUN_MUSHROOM_BLUE = BLOCKS.register("fun_mushroom_blue",
            () -> new FunPlantBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.FUNGUS)));

    public static final RegistryObject<Block> FUN_MUSHROOM_RED = BLOCKS.register("fun_mushroom_red",
            () -> new FunPlantBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.FUNGUS)));

    public static final RegistryObject<Block> FUN_YELLOW_FLOWER = BLOCKS.register("fun_yellow_flower",
            () -> new FunFlowerBlock(FunFlowerBlock.FlowerVariant.YELLOW_DAISY, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .noCollission()
                    .instabreak()
                    .noOcclusion()
                    .sound(SoundType.GRASS)));

    public static final RegistryObject<Block> FUN_BLUE_FLOWER = BLOCKS.register("fun_blue_flower",
            () -> new FunFlowerBlock(FunFlowerBlock.FlowerVariant.BLUE_DAISY, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .noCollission()
                    .instabreak()
                    .noOcclusion()
                    .sound(SoundType.GRASS)));

    public static final RegistryObject<Block> FUN_FLAT_RED_FLOWER = BLOCKS.register("fun_flat_red_flower",
            () -> new FunFlowerBlock(FunFlowerBlock.FlowerVariant.FLAT_RED, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .noCollission()
                    .instabreak()
                    .noOcclusion()
                    .sound(SoundType.GRASS)));

    public static final RegistryObject<Block> FUN_FLAT_BLUE_FLOWER = BLOCKS.register("fun_flat_blue_flower",
            () -> new FunFlowerBlock(FunFlowerBlock.FlowerVariant.FLAT_BLUE, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .noCollission()
                    .instabreak()
                    .noOcclusion()
                    .sound(SoundType.GRASS)));

    public static final RegistryObject<Block> FUN_FLAT_WHITE_FLOWER = BLOCKS.register("fun_flat_white_flower",
            () -> new FunFlowerBlock(FunFlowerBlock.FlowerVariant.FLAT_WHITE, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SNOW)
                    .noCollission()
                    .instabreak()
                    .noOcclusion()
                    .sound(SoundType.GRASS)));

    public static final RegistryObject<Block> MAZE_BRICK = BLOCKS.register("maze_brick",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .sound(SoundType.STONE)));

    public static final RegistryObject<Block> MAZE_FLOOR = BLOCKS.register("maze_floor",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .sound(SoundType.WOOD)));

    public static final RegistryObject<Block> MAZE_CEILING = BLOCKS.register("maze_ceiling",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GRAY)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .sound(SoundType.STONE)));

    public static final RegistryObject<Block> MAZE_PORTAL = BLOCKS.register("maze_portal",
            () -> new MazePortalBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .noCollission()
                    .noOcclusion()
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .lightLevel(s -> 9)
                    .sound(SoundType.GLASS)));

    public static final RegistryObject<Block> MAZE_TELEPORTER = BLOCKS.register("maze_teleporter",
            () -> new MazeTeleporterBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .lightLevel(s -> 12)
                    .sound(SoundType.AMETHYST)));

    // v2.2.0 blocks: The Error, The Time, and Glitch Block
    public static final RegistryObject<Block> ERROR_GROUND = BLOCKS.register("error_ground",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_MAGENTA)
                    .strength(0.8F)
                    .sound(SoundType.STONE)));

    public static final RegistryObject<Block> ERROR_SOIL = BLOCKS.register("error_soil",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.7F)
                    .sound(SoundType.GRAVEL)));

    public static final RegistryObject<Block> GLITCH_BLOCK = BLOCKS.register("glitch_block",
            () -> new com.underworld.mod.block.GlitchBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_MAGENTA)
                    .strength(1.5F, 6.0F)
                    .noOcclusion()
                    .lightLevel(s -> 6)
                    .sound(SoundType.AMETHYST)));

    public static final RegistryObject<Block> CLOCK_FACE_BLOCK = BLOCKS.register("clock_face_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SAND)
                    .strength(1.6F, 6.0F)
                    .sound(SoundType.WOOD)));

    public static final RegistryObject<Block> CLOCK_RIM_BLOCK = BLOCKS.register("clock_rim_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_BLACK)
                    .strength(2.4F, 8.0F)
                    .sound(SoundType.METAL)));

    public static final RegistryObject<Block> TIME_PORTAL = BLOCKS.register("time_portal",
            () -> new com.underworld.mod.block.TimePortalBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .noCollission()
                    .noOcclusion()
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .lightLevel(s -> 13)
                    .sound(SoundType.GLASS)));

    // v2.4.0 blocks: The Plains, The Wind Ocean, The XP, The Browser & Computer
    public static final RegistryObject<Block> PLAINS_GRASS_BLOCK = BLOCKS.register("plains_grass_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GRASS)
                    .strength(0.6F)
                    .sound(SoundType.GRASS)));

    public static final RegistryObject<Block> PLAINS_PATH_BLOCK = BLOCKS.register("plains_path_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(0.8F)
                    .sound(SoundType.GRAVEL)));

    public static final RegistryObject<Block> PLAINS_PORTAL_GRASS = BLOCKS.register("plains_portal_grass",
            () -> new com.underworld.mod.block.PlainsPortalBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(1.0F)
                    .lightLevel(s -> 7)
                    .sound(SoundType.GRASS)));

    public static final RegistryObject<Block> PYLON_STEEL = BLOCKS.register("pylon_steel",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(3.0F, 8.0F)
                    .sound(SoundType.METAL)));

    public static final RegistryObject<Block> WIND_TURBINE_PORTAL = BLOCKS.register("wind_turbine_portal",
            () -> new com.underworld.mod.block.WindTurbinePortalBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .noCollission()
                    .noOcclusion()
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .lightLevel(s -> 12)
                    .sound(SoundType.GLASS)));

    public static final RegistryObject<Block> TURBINE_PILLAR = BLOCKS.register("turbine_pillar",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SNOW)
                    .strength(2.5F, 8.0F)
                    .sound(SoundType.METAL)));

    public static final RegistryObject<Block> TURBINE_BLADE = BLOCKS.register("turbine_blade",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SNOW)
                    .strength(1.8F, 6.0F)
                    .sound(SoundType.METAL)));

    public static final RegistryObject<Block> OIL_RIG_STEEL = BLOCKS.register("oil_rig_steel",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5F, 10.0F)
                    .sound(SoundType.METAL)));

    public static final RegistryObject<Block> OIL_RIG_PORTAL = BLOCKS.register("oil_rig_portal",
            () -> new com.underworld.mod.block.OilRigPortalBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .noCollission()
                    .noOcclusion()
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .lightLevel(s -> 14)
                    .sound(SoundType.GLASS)));

    public static final RegistryObject<Block> XP_GRASS_BLOCK = BLOCKS.register("xp_grass_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .strength(0.6F)
                    .sound(SoundType.GRASS)));

    public static final RegistryObject<Block> XP_ICON_BLOCK = BLOCKS.register("xp_icon_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .strength(0.8F)
                    .lightLevel(s -> 5)
                    .sound(SoundType.GLASS)));

    public static final RegistryObject<Block> XP_ICON_FALLEN = BLOCKS.register("xp_icon_fallen",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(1.0F)
                    .sound(SoundType.STONE)));

    public static final RegistryObject<Block> CHROME_PORTAL = BLOCKS.register("chrome_portal",
            () -> new com.underworld.mod.block.ChromePortalBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .noCollission()
                    .noOcclusion()
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .lightLevel(s -> 14)
                    .sound(SoundType.GLASS)));

    public static final RegistryObject<Block> BROWSER_TILE = BLOCKS.register("browser_tile",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .strength(1.2F)
                    .sound(SoundType.STONE)));

    public static final RegistryObject<Block> BROWSER_BAR_BLOCK = BLOCKS.register("browser_bar_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GRAY)
                    .strength(1.6F)
                    .sound(SoundType.METAL)));

    public static final RegistryObject<Block> SITE_YOUTUBE_BLOCK = BLOCKS.register("site_youtube_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .strength(1.2F)
                    .lightLevel(s -> 4)
                    .sound(SoundType.GLASS)));

    public static final RegistryObject<Block> SITE_ROBLOX_BLOCK = BLOCKS.register("site_roblox_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GRAY)
                    .strength(1.2F)
                    .sound(SoundType.STONE)));

    public static final RegistryObject<Block> SITE_STEAM_BLOCK = BLOCKS.register("site_steam_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .strength(1.4F)
                    .sound(SoundType.METAL)));

    public static final RegistryObject<Block> SITE_REDDIT_BLOCK = BLOCKS.register("site_reddit_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(1.2F)
                    .sound(SoundType.STONE)));

    public static final RegistryObject<Block> SITE_GLITCH_BLOCK = BLOCKS.register("site_glitch_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(1.8F)
                    .lightLevel(s -> 4)
                    .sound(SoundType.AMETHYST)));

    public static final RegistryObject<Block> DOWNLOAD_BUTTON_BLOCK = BLOCKS.register("download_button_block",
            () -> new com.underworld.mod.block.DownloadButtonBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .lightLevel(s -> 15)
                    .sound(SoundType.AMETHYST)));

    public static final RegistryObject<Block> BROWSER_EXIT_PORTAL = BLOCKS.register("browser_exit_portal",
            () -> new com.underworld.mod.block.BrowserExitPortalBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .noCollission()
                    .noOcclusion()
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .lightLevel(s -> 15)
                    .sound(SoundType.GLASS)));

    public static final RegistryObject<Block> COMPUTER = BLOCKS.register("computer",
            () -> new com.underworld.mod.block.ComputerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SAND)
                    .strength(1.8F, 6.0F)
                    .lightLevel(s -> 6)
                    .sound(SoundType.METAL)));

    private ModBlocks() {}
}
