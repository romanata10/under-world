package com.underworld.mod.world.structure.lore;

import com.underworld.mod.registry.ModBlocks;
import com.underworld.mod.world.structure.StructureBuildUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public final class LoreComputerDeskPiece {
    private LoreComputerDeskPiece() {}

    public static void placeAt(ServerLevel level, BlockPos groundPos) {
        BoundingBox box = new BoundingBox(
                groundPos.getX() - 4, groundPos.getY() - 5, groundPos.getZ() - 4,
                groundPos.getX() + 4, groundPos.getY() + 6, groundPos.getZ() + 4
        );

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                StructureBuildUtil.fillFoundationSolid(level, box, groundPos.offset(dx, -1, dz), 4, Blocks.DIRT.defaultBlockState());
            }
        }

        StructureBuildUtil.fillBox(level, box,
                groundPos.getX() - 2, groundPos.getY(), groundPos.getZ() - 2,
                groundPos.getX() + 2, groundPos.getY(), groundPos.getZ() + 2,
                Blocks.DARK_OAK_PLANKS.defaultBlockState());
        StructureBuildUtil.fillBox(level, box,
                groundPos.getX() - 2, groundPos.getY() + 1, groundPos.getZ() - 2,
                groundPos.getX() + 2, groundPos.getY() + 3, groundPos.getZ() + 2,
                Blocks.AIR.defaultBlockState());

        // Desk table + chair + active CRT Computer terminal
        level.setBlock(groundPos.offset(-1, 1, -1), Blocks.OAK_FENCE.defaultBlockState(), 3);
        level.setBlock(groundPos.offset(1, 1, -1), Blocks.OAK_FENCE.defaultBlockState(), 3);
        level.setBlock(groundPos.offset(0, 1, -1), Blocks.OAK_PLANKS.defaultBlockState(), 3);
        level.setBlock(groundPos.offset(0, 2, -1),
                ModBlocks.COMPUTER.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH), 3);
        level.setBlock(groundPos.offset(1, 2, -1), Blocks.REDSTONE_TORCH.defaultBlockState(), 3);
        level.setBlock(groundPos.offset(0, 1, 1), Blocks.OAK_STAIRS.defaultBlockState(), 3);

        // Warning sign + troubleshooting lectern book
        StructureBuildUtil.placeWallOrStandingSign(level, box, groundPos.offset(-2, 1, 0), Direction.EAST,
                DyeColor.WHITE, false,
                "sign.under_world.computer_desk.1", "sign.under_world.computer_desk.2",
                "sign.under_world.computer_desk.3", "sign.under_world.computer_desk.4");

        ItemStack book = StructureBuildUtil.createLoreBook(
                "computer_desk", "PC Troubleshooting - Admin_Max", "Admin_Max", 3);
        StructureBuildUtil.placeLecternWithBook(level, box, groundPos.offset(2, 1, 0), Direction.WEST, book);
    }
}
