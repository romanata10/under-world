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
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Based on image-12.png: terraced modern-alpha oak-plank & grass clan compound
 * with front fenced lawn terrace, right-side rectangular pool, cantilevered
 * upper glass-windowed rooms with corner torches, and a hidden secret basement
 * hiding the server admin's confession note.
 */
public final class LoreClanBasePiece {
    private LoreClanBasePiece() {}

    public static void placeAt(ServerLevel level, BlockPos origin) {
        BoundingBox box = new BoundingBox(
                origin.getX() - 9, origin.getY() - 6, origin.getZ() - 8,
                origin.getX() + 9, origin.getY() + 10, origin.getZ() + 8
        );
        build(level, level.getRandom(), box, origin);
    }

    public static void build(ServerLevelAccessor level, RandomSource random, BoundingBox box, BlockPos origin) {
        int ox = origin.getX();
        int oy = origin.getY();
        int oz = origin.getZ();

        // Clear upper airspace so base sits cleanly above terrain
        StructureBuildUtil.fillBox(level, box, ox - 7, oy + 1, oz - 6, ox + 7, oy + 9, oz + 6, Blocks.AIR.defaultBlockState());

        // Stone-brick / oak pillars supporting the elevated terrace over cliffside/hills
        int[][] pillars = {
                {ox - 7, oz + 5}, {ox - 1, oz + 5}, {ox + 7, oz + 5},
                {ox - 7, oz - 5}, {ox + 7, oz - 5}, {ox + 2, oz - 5}, {ox - 3, oz}
        };
        for (int[] p : pillars) {
            StructureBuildUtil.fillFoundationSolid(level, box, new BlockPos(p[0], oy, p[1]), 10, Blocks.COBBLESTONE.defaultBlockState());
        }

        // Main terraced platform at y = oy
        StructureBuildUtil.fillBox(level, box, ox - 7, oy, oz - 5, ox + 7, oy, oz + 5, Blocks.OAK_PLANKS.defaultBlockState());
        // Left-front grass lawn courtyard (image-12.png left courtyard)
        StructureBuildUtil.fillBox(level, box, ox - 6, oy, oz + 1, ox - 1, oy, oz + 4, Blocks.GRASS_BLOCK.defaultBlockState());

        // Right-side rectangular courtyard pool (image-12.png right-side pool)
        StructureBuildUtil.fillBox(level, box, ox + 2, oy - 2, oz + 1, ox + 6, oy - 2, oz + 4, Blocks.SANDSTONE.defaultBlockState());
        StructureBuildUtil.fillBox(level, box, ox + 2, oy - 1, oz + 1, ox + 6, oy, oz + 4, Blocks.WATER.defaultBlockState());
        // Pool rim
        for (int x = ox + 1; x <= ox + 7; x++) {
            StructureBuildUtil.setIfInside(level, box, new BlockPos(x, oy, oz + 5), Blocks.OAK_PLANKS.defaultBlockState());
        }
        for (int z = oz + 1; z <= oz + 5; z++) {
            StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 7, oy, z), Blocks.OAK_PLANKS.defaultBlockState());
        }

        // Perimeter wooden fence railing on front & sides
        for (int x = ox - 7; x <= ox + 7; x++) {
            if (x == 0 || x == 1) continue; // front stair gap
            StructureBuildUtil.setIfInside(level, box, new BlockPos(x, oy + 1, oz + 5), Blocks.OAK_FENCE.defaultBlockState());
        }
        for (int z = oz - 4; z <= oz + 4; z++) {
            StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 7, oy + 1, z), Blocks.OAK_FENCE.defaultBlockState());
            StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 7, oy + 1, z), Blocks.OAK_FENCE.defaultBlockState());
        }
        // Entrance cobble steps leading up at x = ox..ox+1, z = oz + 5..6
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox, oy, oz + 6), Blocks.OAK_SLAB.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 1, oy, oz + 6), Blocks.OAK_SLAB.defaultBlockState());

        // Main modern-alpha multi-story oak plank & glass house on rear terrace (x=-6..5, z=-5..0)
        StructureBuildUtil.fillBox(level, box, ox - 6, oy + 1, oz - 5, ox + 5, oy + 4, oz - 5, Blocks.OAK_PLANKS.defaultBlockState());
        StructureBuildUtil.fillBox(level, box, ox - 6, oy + 1, oz - 5, ox - 6, oy + 4, oz, Blocks.OAK_PLANKS.defaultBlockState());
        StructureBuildUtil.fillBox(level, box, ox + 5, oy + 1, oz - 5, ox + 5, oy + 4, oz, Blocks.OAK_PLANKS.defaultBlockState());
        StructureBuildUtil.fillBox(level, box, ox - 6, oy + 1, oz, ox + 5, oy + 4, oz, Blocks.OAK_PLANKS.defaultBlockState());

        // Broad front & side horizontal glass bands (characteristic of modern alpha builds in image-12.png)
        for (int x = ox - 5; x <= ox - 1; x++) {
            StructureBuildUtil.setIfInside(level, box, new BlockPos(x, oy + 2, oz), Blocks.GLASS_PANE.defaultBlockState());
            StructureBuildUtil.setIfInside(level, box, new BlockPos(x, oy + 3, oz), Blocks.GLASS_PANE.defaultBlockState());
        }
        for (int x = ox + 2; x <= ox + 4; x++) {
            StructureBuildUtil.setIfInside(level, box, new BlockPos(x, oy + 2, oz), Blocks.GLASS_PANE.defaultBlockState());
            StructureBuildUtil.setIfInside(level, box, new BlockPos(x, oy + 3, oz), Blocks.GLASS_PANE.defaultBlockState());
        }
        // Doorway into clan hall (x=ox, z=oz)
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox, oy + 1, oz),
                Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH).setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox, oy + 2, oz),
                Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH).setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));

        // Second cantilevered upper wing jutting forward on left (x=-6..2, z=-4..1, y=4..7)
        StructureBuildUtil.fillBox(level, box, ox - 6, oy + 4, oz - 4, ox + 3, oy + 4, oz + 1, Blocks.OAK_PLANKS.defaultBlockState());
        StructureBuildUtil.fillBox(level, box, ox - 6, oy + 5, oz - 4, ox + 3, oy + 7, oz - 4, Blocks.OAK_PLANKS.defaultBlockState());
        StructureBuildUtil.fillBox(level, box, ox - 6, oy + 5, oz - 4, ox - 6, oy + 7, oz + 1, Blocks.OAK_PLANKS.defaultBlockState());
        StructureBuildUtil.fillBox(level, box, ox + 3, oy + 5, oz - 4, ox + 3, oy + 7, oz + 1, Blocks.OAK_PLANKS.defaultBlockState());
        StructureBuildUtil.fillBox(level, box, ox - 6, oy + 5, oz + 1, ox + 3, oy + 7, oz + 1, Blocks.OAK_PLANKS.defaultBlockState());
        for (int x = ox - 5; x <= ox + 2; x++) {
            StructureBuildUtil.setIfInside(level, box, new BlockPos(x, oy + 6, oz + 1), Blocks.GLASS_PANE.defaultBlockState());
        }
        // Flat alpha roof + oak log corner trim + corner torches (matching image-12.png!)
        StructureBuildUtil.fillBox(level, box, ox - 6, oy + 8, oz - 4, ox + 3, oy + 8, oz + 1, Blocks.OAK_SLAB.defaultBlockState());
        StructureBuildUtil.fillBox(level, box, ox + 3, oy + 4, oz - 5, ox + 5, oy + 4, oz, Blocks.OAK_SLAB.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 6, oy + 9, oz + 1), Blocks.TORCH.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 3, oy + 9, oz + 1), Blocks.TORCH.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 6, oy + 9, oz - 4), Blocks.TORCH.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 3, oy + 9, oz - 4), Blocks.TORCH.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 7, oy + 2, oz + 5), Blocks.TORCH.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 7, oy + 2, oz + 5), Blocks.TORCH.defaultBlockState());

        // Clan base exterior/interior signs
        StructureBuildUtil.placeWallSign(level, box, new BlockPos(ox + 1, oy + 2, oz + 1), Direction.SOUTH,
                Blocks.OAK_WALL_SIGN, DyeColor.BLACK, false,
                "sign.under_world.clan.1", "sign.under_world.clan.2",
                "sign.under_world.clan.3", "sign.under_world.clan.4");

        // Interior clan gear, ladder up to 2nd floor, beds & clan chest
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 5, oy + 1, oz - 4), Blocks.CRAFTING_TABLE.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 4, oy + 1, oz - 4), Blocks.FURNACE.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 4, oy + 1, oz - 4),
                Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.SOUTH).setValue(BedBlock.PART, BedPart.HEAD));
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 4, oy + 1, oz - 3),
                Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.SOUTH).setValue(BedBlock.PART, BedPart.FOOT));
        for (int y = oy + 1; y <= oy + 4; y++) {
            StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 2, y, oz - 4),
                    Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));
        }

        StructureBuildUtil.placeChestWithItems(level, box, new BlockPos(ox - 3, oy + 1, oz - 4), Direction.SOUTH, random, null,
                new ItemStack(Items.COOKED_BEEF, 6),
                new ItemStack(Items.IRON_SWORD, 1),
                new ItemStack(Items.OAK_PLANKS, 32),
                new ItemStack(Items.TORCH, 12));

        // === HIDDEN SECRET BASEMENT BELOW FLOOR (y = oy - 4 .. oy - 1) ===
        StructureBuildUtil.fillBox(level, box, ox - 5, oy - 5, oz - 5, ox + 1, oy - 1, oz - 1, Blocks.STONE_BRICKS.defaultBlockState());
        StructureBuildUtil.fillBox(level, box, ox - 4, oy - 4, oz - 4, ox, oy - 2, oz - 2, Blocks.AIR.defaultBlockState());
        // Mossy/cracked cellar touches & lone dim redstone torch
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 2, oy - 5, oz - 3), Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 4, oy - 3, oz - 2), Blocks.REDSTONE_TORCH.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox, oy - 3, oz - 4), ModBlocks.GLITCH_BLOCK.get().defaultBlockState());

        // Concealed trapdoor behind interior corner + ladder down into secret basement
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 4, oy, oz - 3),
                Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING, Direction.SOUTH));
        for (int y = oy - 4; y <= oy - 1; y++) {
            StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 4, y, oz - 3),
                    Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));
        }

        // Server Admin's Secret Confession Note on a Lectern + warning wall sign
        ItemStack confessionBook = StructureBuildUtil.createLoreBook(
                "clan_base_secret", "ADMIN CONFESSION [BACKUP_14]", "ServerAdmin", 4);
        StructureBuildUtil.placeLecternWithBook(level, box, new BlockPos(ox - 1, oy - 4, oz - 3), Direction.WEST, confessionBook);
        StructureBuildUtil.placeWallSign(level, box, new BlockPos(ox - 1, oy - 2, oz - 2), Direction.NORTH,
                Blocks.OAK_WALL_SIGN, DyeColor.RED, true,
                "sign.under_world.clan_secret.1", "sign.under_world.clan_secret.2",
                "sign.under_world.clan_secret.3", "sign.under_world.clan_secret.4");
    }
}
