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
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Hidden underground dugout beneath a grass-covered earthen hatch,
 * lit dimly by a redstone torch with iron bars, rations, warning signs, and survival diary.
 */
public final class LoreDugoutPiece {
    private LoreDugoutPiece() {}

    public static void placeAt(ServerLevel level, BlockPos origin) {
        BoundingBox box = new BoundingBox(
                origin.getX() - 5, origin.getY() - 6, origin.getZ() - 5,
                origin.getX() + 5, origin.getY() + 4, origin.getZ() + 5
        );
        build(level, level.getRandom(), box, origin);
    }

    public static void build(ServerLevelAccessor level, RandomSource random, BoundingBox box, BlockPos origin) {
        int ox = origin.getX();
        int oy = origin.getY();
        int oz = origin.getZ();

        // Subtle camouflaged grassy dirt hump on surface with disguised hatch
        StructureBuildUtil.fillBox(level, box, ox - 3, oy, oz - 3, ox + 3, oy, oz + 3, Blocks.GRASS_BLOCK.defaultBlockState());
        StructureBuildUtil.fillBox(level, box, ox - 2, oy + 1, oz - 2, ox + 2, oy + 1, oz + 2, Blocks.GRASS_BLOCK.defaultBlockState());
        StructureBuildUtil.fillBox(level, box, ox - 1, oy + 2, oz - 1, ox + 1, oy + 2, oz + 1, Blocks.AIR.defaultBlockState());

        // Underground bunker shell (y = oy - 5 .. oy)
        StructureBuildUtil.fillBox(level, box, ox - 4, oy - 5, oz - 4, ox + 4, oy, oz + 4, Blocks.COBBLESTONE.defaultBlockState());
        StructureBuildUtil.fillBox(level, box, ox - 3, oy - 4, oz - 3, ox + 3, oy - 2, oz + 3, Blocks.AIR.defaultBlockState());
        // Oak plank floor & support beams
        StructureBuildUtil.fillBox(level, box, ox - 3, oy - 5, oz - 3, ox + 3, oy - 5, oz + 3, Blocks.OAK_PLANKS.defaultBlockState());
        for (int y = oy - 4; y <= oy - 2; y++) {
            StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 3, y, oz - 3), Blocks.OAK_LOG.defaultBlockState());
            StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 3, y, oz - 3), Blocks.OAK_LOG.defaultBlockState());
            StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 3, y, oz + 3), Blocks.OAK_LOG.defaultBlockState());
            StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 3, y, oz + 3), Blocks.OAK_LOG.defaultBlockState());
        }

        // Hidden trapdoor hatch + cobble chimney shaft down at (ox, oy + 1 .. oy - 4, oz + 2)
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox, oy + 1, oz + 2),
                Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING, Direction.NORTH));
        for (int y = oy - 4; y <= oy; y++) {
            StructureBuildUtil.setIfInside(level, box, new BlockPos(ox, y, oz + 2),
                    Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH));
        }

        // Cramped bunker details: iron bars barricade, bed, furnace, chest, dim redstone torch + glitch spot
        StructureBuildUtil.fillBox(level, box, ox - 2, oy - 4, oz - 1, ox, oy - 3, oz - 1, Blocks.IRON_BARS.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 2, oy - 4, oz - 2),
                Blocks.GRAY_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH).setValue(BedBlock.PART, BedPart.HEAD));
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 2, oy - 4, oz - 1),
                Blocks.GRAY_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH).setValue(BedPart.FOOT));
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 2, oy - 4, oz - 3), Blocks.FURNACE.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 1, oy - 4, oz - 3), Blocks.CRAFTING_TABLE.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 3, oy - 3, oz), Blocks.REDSTONE_TORCH.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 3, oy - 3, oz), Blocks.TORCH.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 4, oy - 3, oz - 2), ModBlocks.GLITCH_BLOCK.get().defaultBlockState());

        // Survival rations + note
        ItemStack journal = StructureBuildUtil.createLoreBook("dugout", "Hideout Entry", "Alex_2014", 3);
        StructureBuildUtil.placeChestWithItems(level, box, new BlockPos(ox + 1, oy - 4, oz - 3), Direction.SOUTH, random, null,
                journal,
                new ItemStack(Items.BREAD, 8),
                new ItemStack(Items.STONE_SWORD, 1),
                new ItemStack(Items.REDSTONE_TORCH, 3),
                new ItemStack(Items.COAL, 10));

        // Bunker wall warning + tiny surface marker sign hidden beside hatch
        StructureBuildUtil.placeWallSign(level, box, new BlockPos(ox, oy - 3, oz - 3), Direction.SOUTH,
                Blocks.OAK_WALL_SIGN, DyeColor.RED, true,
                "sign.under_world.dugout.1", "sign.under_world.dugout.2",
                "sign.under_world.dugout.3", "sign.under_world.dugout.4");
        StructureBuildUtil.placeStandingSign(level, box, new BlockPos(ox + 1, oy + 2, oz + 2), 8,
                Blocks.OAK_SIGN, DyeColor.BLACK, false,
                "sign.under_world.dugout_top.1", "sign.under_world.dugout_top.2", "", "");
    }
}
