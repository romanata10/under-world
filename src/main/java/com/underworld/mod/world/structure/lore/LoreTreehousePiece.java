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
 * Abandoned high oak treehouse perched 7 blocks up in a thick oak canopy,
 * complete with ladder trunk, glass pane lookout windows, supplies, and a survivor's panicked journal.
 */
public final class LoreTreehousePiece {
    private LoreTreehousePiece() {}

    public static void placeAt(ServerLevel level, BlockPos origin) {
        BoundingBox box = new BoundingBox(
                origin.getX() - 6, origin.getY() - 2, origin.getZ() - 6,
                origin.getX() + 6, origin.getY() + 14, origin.getZ() + 6
        );
        build(level, level.getRandom(), box, origin);
    }

    public static void build(ServerLevelAccessor level, RandomSource random, BoundingBox box, BlockPos origin) {
        int ox = origin.getX();
        int oy = origin.getY();
        int oz = origin.getZ();

        // Ground foundation under trunk
        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                StructureBuildUtil.fillFoundationSolid(level, box, new BlockPos(ox + dx, oy, oz + dz), 5, Blocks.DIRT.defaultBlockState());
            }
        }
        // Clear upper canopy area cleanly
        StructureBuildUtil.fillBox(level, box, ox - 5, oy + 1, oz - 5, ox + 5, oy + 13, oz + 5, Blocks.AIR.defaultBlockState());

        // Thick 2x2 oak trunk
        StructureBuildUtil.fillBox(level, box, ox, oy, oz, ox + 1, oy + 9, oz + 1, Blocks.OAK_LOG.defaultBlockState());

        // Ladder climbing the south face of trunk (z = oz + 2)
        for (int y = oy + 1; y <= oy + 6; y++) {
            StructureBuildUtil.setIfInside(level, box, new BlockPos(ox, y, oz + 2),
                    Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));
        }

        // Oak treehouse floor platform at oy + 6 (ox-3..ox+4, oz-3..oz+4)
        StructureBuildUtil.fillBox(level, box, ox - 3, oy + 6, oz - 3, ox + 4, oy + 6, oz + 4, Blocks.OAK_PLANKS.defaultBlockState());
        // Hatch trapdoor right above ladder
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox, oy + 6, oz + 2),
                Blocks.OAK_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING, Direction.SOUTH));

        // Porch railing around outer balcony
        for (int x = ox - 3; x <= ox + 4; x++) {
            StructureBuildUtil.setIfInside(level, box, new BlockPos(x, oy + 7, oz + 4), Blocks.OAK_FENCE.defaultBlockState());
            StructureBuildUtil.setIfInside(level, box, new BlockPos(x, oy + 7, oz - 3), Blocks.OAK_FENCE.defaultBlockState());
        }
        for (int z = oz - 3; z <= oz + 4; z++) {
            StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 3, oy + 7, z), Blocks.OAK_FENCE.defaultBlockState());
            StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 4, oy + 7, z), Blocks.OAK_FENCE.defaultBlockState());
        }

        // Cozy cabin walls (ox-2..ox+3, oz-2..oz+2, y=oy+7..oy+9)
        StructureBuildUtil.fillBox(level, box, ox - 2, oy + 7, oz - 2, ox + 3, oy + 9, oz - 2, Blocks.OAK_PLANKS.defaultBlockState());
        StructureBuildUtil.fillBox(level, box, ox - 2, oy + 7, oz + 2, ox + 3, oy + 9, oz + 2, Blocks.OAK_PLANKS.defaultBlockState());
        StructureBuildUtil.fillBox(level, box, ox - 2, oy + 7, oz - 2, ox - 2, oy + 9, oz + 2, Blocks.OAK_PLANKS.defaultBlockState());
        StructureBuildUtil.fillBox(level, box, ox + 3, oy + 7, oz - 2, ox + 3, oy + 9, oz + 2, Blocks.OAK_PLANKS.defaultBlockState());
        StructureBuildUtil.fillBox(level, box, ox - 1, oy + 7, oz - 1, ox + 2, oy + 9, oz + 1, Blocks.AIR.defaultBlockState());
        // Preserve trunk pillars inside cabin corners cleanly or clear inner space
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox, oy + 7, oz + 2), Blocks.AIR.defaultBlockState());

        // Glass lookout windows
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 1, oy + 8, oz - 2), Blocks.GLASS_PANE.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 1, oy + 8, oz - 2), Blocks.GLASS_PANE.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 3, oy + 8, oz), Blocks.GLASS_PANE.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 2, oy + 8, oz), Blocks.GLASS_PANE.defaultBlockState());

        // Door onto front balcony
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 1, oy + 7, oz + 2),
                Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH).setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 1, oy + 8, oz + 2),
                Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH).setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));

        // Roof & leafy oak canopy above/around treehouse
        StructureBuildUtil.fillBox(level, box, ox - 3, oy + 10, oz - 3, ox + 4, oy + 10, oz + 3, Blocks.SPRUCE_SLAB.defaultBlockState());
        for (int x = ox - 4; x <= ox + 5; x++) {
            for (int z = oz - 4; z <= oz + 5; z++) {
                if (Math.abs(x - ox) <= 4 && Math.abs(z - oz) <= 4 && random.nextInt(3) != 0) {
                    StructureBuildUtil.setIfInside(level, box, new BlockPos(x, oy + 11, z), Blocks.OAK_LEAVES.defaultBlockState());
                }
                if (Math.abs(x - ox) <= 2 && Math.abs(z - oz) <= 2) {
                    StructureBuildUtil.setIfInside(level, box, new BlockPos(x, oy + 12, z), Blocks.OAK_LEAVES.defaultBlockState());
                }
            }
        }
        // Corner glitch leaf anomaly
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 3, oy + 11, oz + 3), ModBlocks.GLITCH_BLOCK.get().defaultBlockState());

        // Interior furnishings: Bed, crafting table, torches, chest + survivor's journal
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 2, oy + 7, oz - 1),
                Blocks.GREEN_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH).setValue(BedBlock.PART, BedPart.HEAD));
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 2, oy + 7, oz),
                Blocks.GREEN_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH).setValue(BedPart.FOOT));
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 1, oy + 7, oz - 1), Blocks.CRAFTING_TABLE.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 1, oy + 8, oz + 1), Blocks.TORCH.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 4, oy + 8, oz + 4), Blocks.TORCH.defaultBlockState());

        ItemStack diary = StructureBuildUtil.createLoreBook("treehouse", "Lookout Notes", "Misha_Play", 3);
        StructureBuildUtil.placeChestWithItems(level, box, new BlockPos(ox - 1, oy + 7, oz), Direction.EAST, random, null,
                diary,
                new ItemStack(Items.APPLE, 5),
                new ItemStack(Items.ARROW, 12),
                new ItemStack(Items.SPYGLASS, 1));

        // Trunk warning sign at eye level on ground
        StructureBuildUtil.placeWallSign(level, box, new BlockPos(ox + 1, oy + 2, oz + 2), Direction.SOUTH,
                Blocks.OAK_WALL_SIGN, DyeColor.RED, false,
                "sign.under_world.treehouse.1", "sign.under_world.treehouse.2",
                "sign.under_world.treehouse.3", "sign.under_world.treehouse.4");
    }
}
