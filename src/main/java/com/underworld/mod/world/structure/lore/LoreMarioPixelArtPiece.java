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
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Nostalgic server pixel art: classic 12x16 wool Super Mario sprite built mid-air
 * on a cobble platform with leftover scaffolding & the builder's last diary.
 */
public final class LoreMarioPixelArtPiece {
    private static final String[] MARIO_ROWS = {
            "...RRRRR....", // y = +16 (top of cap)
            "..RRRRRRRRR.", // y = +15
            "..BBBSSBS...", // y = +14
            ".BSBSSSBS...", // y = +13
            ".BSBBSSSB...", // y = +12
            ".BBSSSSBBBB.", // y = +11
            "...SSSSSSS..", // y = +10
            "..RRBRRR....", // y = +9
            ".RRRBRRBRRR.", // y = +8
            "RRRRBBBB.RRR", // y = +7
            "SSRBYYYYBRSS", // y = +6
            "SSSBBBBBBSSS", // y = +5
            "SS.BBBBBB.SS", // y = +4
            "...BBB..BBB.", // y = +3
            "..NNN....NNN", // y = +2
            ".NNNN....NNN"  // y = +1
    };

    private LoreMarioPixelArtPiece() {}

    public static void placeAt(ServerLevel level, BlockPos origin) {
        BoundingBox box = new BoundingBox(
                origin.getX() - 9, origin.getY() - 2, origin.getZ() - 5,
                origin.getX() + 10, origin.getY() + 19, origin.getZ() + 6
        );
        build(level, level.getRandom(), box, origin);
    }

    public static void build(ServerLevelAccessor level, RandomSource random, BoundingBox box, BlockPos origin) {
        int ox = origin.getX();
        int oy = origin.getY();
        int oz = origin.getZ();

        // Cobblestone / mossy plinth base
        for (int x = ox - 7; x <= ox + 8; x++) {
            for (int z = oz - 2; z <= oz + 3; z++) {
                StructureBuildUtil.fillFoundationSolid(level, box, new BlockPos(x, oy, z), 6, Blocks.STONE.defaultBlockState());
                BlockState top = random.nextInt(5) == 0 ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState();
                StructureBuildUtil.setIfInside(level, box, new BlockPos(x, oy, z), top);
            }
        }
        StructureBuildUtil.fillBox(level, box, ox - 7, oy + 1, oz - 2, ox + 8, oy + 18, oz + 3, Blocks.AIR.defaultBlockState());

        // 16-row nostalgic wool pixel art facing South (+Z)
        for (int r = 0; r < MARIO_ROWS.length; r++) {
            String row = MARIO_ROWS[r];
            int y = oy + 16 - r;
            for (int c = 0; c < row.length(); c++) {
                char ch = row.charAt(c);
                if (ch == '.') continue;
                BlockState state = mapColor(ch);
                // Replace one pixel near the edge with a tiny glitch anomaly
                if (r == 4 && c == 8) {
                    state = ModBlocks.GLITCH_BLOCK.get().defaultBlockState();
                }
                StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 6 + c, y, oz), state);
            }
        }

        // Unfinished oak plank & ladder scaffolding on the right side (as if builder fled mid-build)
        for (int y = oy + 1; y <= oy + 10; y++) {
            StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 7, y, oz + 1), Blocks.OAK_FENCE.defaultBlockState());
            StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 6, y, oz + 1),
                    Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));
        }
        StructureBuildUtil.fillBox(level, box, ox + 5, oy + 11, oz, ox + 8, oy + 11, oz + 1, Blocks.OAK_PLANKS.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 8, oy + 12, oz + 1), Blocks.TORCH.defaultBlockState());

        // Memorial signs + abandoned wool supply chest
        StructureBuildUtil.placeStandingSign(level, box, new BlockPos(ox - 1, oy + 1, oz + 2), 0,
                Blocks.OAK_SIGN, DyeColor.RED, false,
                "sign.under_world.mario.1", "sign.under_world.mario.2",
                "sign.under_world.mario.3", "sign.under_world.mario.4");

        ItemStack book = StructureBuildUtil.createLoreBook("mario_art", "Pixel Build Notes", "Dima_2015", 3);
        StructureBuildUtil.placeChestWithItems(level, box, new BlockPos(ox + 2, oy + 1, oz + 2), Direction.SOUTH, random, null,
                book,
                new ItemStack(Items.RED_WOOL, 19),
                new ItemStack(Items.BLUE_WOOL, 14),
                new ItemStack(Items.LADDER, 11),
                new ItemStack(Items.SHEARS, 1));
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 3, oy + 1, oz + 2), Blocks.TORCH.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 4, oy + 1, oz + 2), Blocks.REDSTONE_TORCH.defaultBlockState());
    }

    private static BlockState mapColor(char ch) {
        return switch (ch) {
            case 'R' -> Blocks.RED_WOOL.defaultBlockState();
            case 'B' -> Blocks.BLUE_WOOL.defaultBlockState();
            case 'S' -> Blocks.WHITE_TERRACOTTA.defaultBlockState();
            case 'Y' -> Blocks.YELLOW_WOOL.defaultBlockState();
            case 'N' -> Blocks.BROWN_WOOL.defaultBlockState();
            default -> Blocks.BLACK_WOOL.defaultBlockState();
        };
    }
}
