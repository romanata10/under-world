package com.underworld.mod.world.structure.lore;

import com.underworld.mod.registry.ModBlocks;
import com.underworld.mod.world.structure.StructureBuildUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Based on image-11.png: nostalgic hillside recessed house with glass front facade,
 * grass overhang roof, left stone staircase, stone walkway with torches,
 * glowstone floor inlays, and stacked interior double chests.
 */
public final class LoreHillsideHousePiece {
    private LoreHillsideHousePiece() {}

    public static void placeAt(ServerLevel level, BlockPos origin) {
        BoundingBox box = new BoundingBox(
                origin.getX() - 6, origin.getY() - 2, origin.getZ() - 5,
                origin.getX() + 6, origin.getY() + 8, origin.getZ() + 6
        );
        build(level, level.getRandom(), box, origin);
    }

    public static void build(ServerLevelAccessor level, RandomSource random, BoundingBox box, BlockPos origin) {
        int ox = origin.getX();
        int oy = origin.getY();
        int oz = origin.getZ();

        // Solidify ground beneath the front & footprint
        for (int x = ox - 5; x <= ox + 5; x++) {
            for (int z = oz - 4; z <= oz + 5; z++) {
                StructureBuildUtil.fillFoundationSolid(level, box, new BlockPos(x, oy, z), 6, Blocks.STONE.defaultBlockState());
            }
        }

        // Clear interior & front courtyard
        StructureBuildUtil.fillBox(level, box, ox - 5, oy + 1, oz - 3, ox + 5, oy + 7, oz + 5, Blocks.AIR.defaultBlockState());

        // Floor & back/side stone hill shell
        StructureBuildUtil.fillBox(level, box, ox - 4, oy, oz - 3, ox + 4, oy, oz + 3, Blocks.STONE.defaultBlockState());
        StructureBuildUtil.fillBox(level, box, ox - 4, oy + 1, oz - 3, ox + 4, oy + 4, oz - 3, Blocks.COBBLESTONE.defaultBlockState());
        StructureBuildUtil.fillBox(level, box, ox - 4, oy + 1, oz - 3, ox - 3, oy + 4, oz + 2, Blocks.STONE.defaultBlockState());
        StructureBuildUtil.fillBox(level, box, ox + 3, oy + 1, oz - 3, ox + 4, oy + 4, oz + 3, Blocks.STONE.defaultBlockState());

        // Glowstone floor inlays inside (matching image-11.png interior)
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 1, oy, oz), Blocks.GLOWSTONE.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 1, oy, oz), Blocks.GLOWSTONE.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 1, oy, oz - 2), Blocks.GLOWSTONE.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 1, oy, oz - 2), Blocks.GLOWSTONE.defaultBlockState());

        // Stone ceiling + stepped grassy hill mound roof overhanging the facade
        StructureBuildUtil.fillBox(level, box, ox - 4, oy + 4, oz - 4, ox + 4, oy + 4, oz + 3, Blocks.COBBLESTONE.defaultBlockState());
        StructureBuildUtil.fillBox(level, box, ox - 5, oy + 5, oz - 4, ox + 5, oy + 5, oz + 3, Blocks.DIRT.defaultBlockState());
        StructureBuildUtil.fillBox(level, box, ox - 5, oy + 5, oz - 4, ox + 5, oy + 5, oz + 3, Blocks.GRASS_BLOCK.defaultBlockState());
        // Upper hillside crest sloping back
        StructureBuildUtil.fillBox(level, box, ox - 4, oy + 6, oz - 4, ox + 4, oy + 6, oz + 1, Blocks.GRASS_BLOCK.defaultBlockState());
        // Overhanging dirt/grass eaves lip on facade just like image-11.png
        for (int x = ox - 3; x <= ox + 3; x++) {
            StructureBuildUtil.setIfInside(level, box, new BlockPos(x, oy + 5, oz + 4), Blocks.GRASS_BLOCK.defaultBlockState());
            StructureBuildUtil.setIfInside(level, box, new BlockPos(x, oy + 4, oz + 4), Blocks.DIRT.defaultBlockState());
        }

        // Front glass facade & oak door (z = oz + 3)
        for (int x = ox - 2; x <= ox + 2; x++) {
            for (int y = oy + 1; y <= oy + 3; y++) {
                StructureBuildUtil.setIfInside(level, box, new BlockPos(x, y, oz + 3), Blocks.GLASS.defaultBlockState());
            }
        }
        // Oak door at ox + 1, z = oz + 3
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 1, oy + 1, oz + 3),
                Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH).setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 1, oy + 2, oz + 3),
                Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH).setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));

        // Front stone-brick walkway + torches + wall sign beside door
        StructureBuildUtil.fillBox(level, box, ox - 3, oy, oz + 4, ox + 3, oy, oz + 5, Blocks.STONE_BRICKS.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 2, oy + 1, oz + 4), Blocks.TORCH.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 2, oy + 1, oz + 4), Blocks.TORCH.defaultBlockState());
        StructureBuildUtil.placeWallSign(level, box, new BlockPos(ox + 2, oy + 2, oz + 4), Direction.SOUTH,
                Blocks.OAK_WALL_SIGN, DyeColor.BLACK, false,
                "sign.under_world.hillside.1", "sign.under_world.hillside.2",
                "sign.under_world.hillside.3", "sign.under_world.hillside.4");

        // Cobblestone / stone-brick staircase descending along the left outer hillside
        for (int s = 0; s < 5; s++) {
            int sy = oy + 4 - s;
            int sz = oz - 1 + s;
            for (int sx = ox - 5; sx <= ox - 4; sx++) {
                StructureBuildUtil.fillColumnDown(level, box, new BlockPos(sx, sy - 1, sz), 6, Blocks.STONE.defaultBlockState());
                StructureBuildUtil.setIfInside(level, box, new BlockPos(sx, sy, sz),
                        Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
                StructureBuildUtil.setIfInside(level, box, new BlockPos(sx, sy + 1, sz), Blocks.AIR.defaultBlockState());
                StructureBuildUtil.setIfInside(level, box, new BlockPos(sx, sy + 2, sz), Blocks.AIR.defaultBlockState());
            }
        }
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 4, oy + 5, oz - 2), Blocks.TORCH.defaultBlockState());

        // Interior furnishings: stacked chests (matching image-11.png) + crafting table + glitch clue
        ItemStack journal = StructureBuildUtil.createLoreBook("hillside_house", "Storage Log", "Kira_99", 3);
        StructureBuildUtil.placeChestWithItems(level, box, new BlockPos(ox + 2, oy + 1, oz - 2), Direction.WEST, random, null,
                journal,
                new ItemStack(Items.COBBLESTONE, 27),
                new ItemStack(Items.TORCH, 9),
                new ItemStack(Items.BREAD, 4));
        StructureBuildUtil.placeChestWithItems(level, box, new BlockPos(ox + 2, oy + 2, oz - 2), Direction.WEST, random, null,
                new ItemStack(Items.IRON_PICKAXE, 1),
                new ItemStack(Items.OAK_PLANKS, 14),
                new ItemStack(Items.REDSTONE, 5));

        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 2, oy + 1, oz - 2), Blocks.CRAFTING_TABLE.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 2, oy + 1, oz - 1), Blocks.FURNACE.defaultBlockState());
        // Unsettling glitch block tucked into the back corner wall
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 3, oy + 2, oz - 2), ModBlocks.GLITCH_BLOCK.get().defaultBlockState());
    }
}
