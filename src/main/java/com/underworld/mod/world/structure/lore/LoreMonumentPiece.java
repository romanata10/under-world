package com.underworld.mod.world.structure.lore;

import com.underworld.mod.registry.ModBlocks;
import com.underworld.mod.world.structure.StructureBuildUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Nostalgic server memorial statue of an unknown early player standing on a stepped
 * stone-brick & mossy pedestal with glowing lectern logs and cryptic warnings.
 */
public final class LoreMonumentPiece {
    private LoreMonumentPiece() {}

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

        for (int x = ox - 4; x <= ox + 4; x++) {
            for (int z = oz - 4; z <= oz + 4; z++) {
                StructureBuildUtil.fillFoundationSolid(level, box, new BlockPos(x, oy, z), 6, Blocks.STONE.defaultBlockState());
                BlockState base = random.nextInt(4) == 0 ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState();
                StructureBuildUtil.setIfInside(level, box, new BlockPos(x, oy, z), base);
            }
        }
        StructureBuildUtil.fillBox(level, box, ox - 4, oy + 1, oz - 4, ox + 4, oy + 12, oz + 4, Blocks.AIR.defaultBlockState());

        // Stepped inner plinth
        StructureBuildUtil.fillBox(level, box, ox - 2, oy + 1, oz - 2, ox + 2, oy + 2, oz + 2, Blocks.STONE_BRICKS.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox, oy + 1, oz + 2),
                Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));

        // Player Statue (Facing South +Z): Boots/Legs, Shirt/Arms, Head
        // Legs (y = 3..5)
        StructureBuildUtil.fillBox(level, box, ox - 1, oy + 3, oz, ox - 1, oy + 5, oz, Blocks.BLUE_TERRACOTTA.defaultBlockState());
        StructureBuildUtil.fillBox(level, box, ox + 1, oy + 3, oz, ox + 1, oy + 5, oz, Blocks.BLUE_TERRACOTTA.defaultBlockState());
        // Torso & gold medallion (y = 6..8)
        StructureBuildUtil.fillBox(level, box, ox - 1, oy + 6, oz, ox + 1, oy + 8, oz, Blocks.CYAN_TERRACOTTA.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox, oy + 7, oz), Blocks.GOLD_BLOCK.defaultBlockState());
        // Arms (y = 6..8)
        StructureBuildUtil.fillBox(level, box, ox - 2, oy + 6, oz, ox - 2, oy + 8, oz, Blocks.STONE_BRICKS.defaultBlockState());
        StructureBuildUtil.fillBox(level, box, ox + 2, oy + 6, oz, ox + 2, oy + 8, oz, Blocks.STONE_BRICKS.defaultBlockState());
        // Head (y = 9..11, 3x2x2 bust)
        StructureBuildUtil.fillBox(level, box, ox - 1, oy + 9, oz - 1, ox + 1, oy + 11, oz + 1, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
        // Statue Eyes (one pure white quartz, one corrupted glitch block!)
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 1, oy + 10, oz + 1), Blocks.QUARTZ_BLOCK.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 1, oy + 10, oz + 1), ModBlocks.GLITCH_BLOCK.get().defaultBlockState());

        // Corner pillars & mismatched torches (one redstone!)
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 4, oy + 1, oz + 4), Blocks.STONE_BRICK_WALL.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 4, oy + 2, oz + 4), Blocks.TORCH.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 4, oy + 1, oz + 4), Blocks.STONE_BRICK_WALL.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 4, oy + 2, oz + 4), Blocks.REDSTONE_TORCH.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 4, oy + 1, oz - 4), Blocks.STONE_BRICK_WALL.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 4, oy + 2, oz - 4), Blocks.TORCH.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 4, oy + 1, oz - 4), Blocks.STONE_BRICK_WALL.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 4, oy + 2, oz - 4), Blocks.TORCH.defaultBlockState());

        // Plaque signs & Lectern memorial book
        StructureBuildUtil.placeWallSign(level, box, new BlockPos(ox, oy + 2, oz + 3), Direction.SOUTH,
                Blocks.OAK_WALL_SIGN, DyeColor.WHITE, true,
                "sign.under_world.monument.1", "sign.under_world.monument.2",
                "sign.under_world.monument.3", "sign.under_world.monument.4");

        ItemStack memorial = StructureBuildUtil.createLoreBook("monument", "Server Archive #1", "Founder_Max", 3);
        StructureBuildUtil.placeLecternWithBook(level, box, new BlockPos(ox, oy + 1, oz + 3), Direction.SOUTH, memorial);
    }
}
