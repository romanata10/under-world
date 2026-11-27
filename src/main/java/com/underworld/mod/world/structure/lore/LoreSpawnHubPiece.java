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
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Classic old multiplayer server Spawn Hub / Rules Plaza: stone-brick courtyard,
 * oak pavilion archways, central glowstone fountain, staff & rules wall signs,
 * starter food chest, and corrupted rulebook.
 */
public final class LoreSpawnHubPiece {
    private LoreSpawnHubPiece() {}

    public static void placeAt(ServerLevel level, BlockPos origin) {
        BoundingBox box = new BoundingBox(
                origin.getX() - 7, origin.getY() - 2, origin.getZ() - 7,
                origin.getX() + 7, origin.getY() + 8, origin.getZ() + 7
        );
        build(level, level.getRandom(), box, origin);
    }

    public static void build(ServerLevelAccessor level, RandomSource random, BoundingBox box, BlockPos origin) {
        int ox = origin.getX();
        int oy = origin.getY();
        int oz = origin.getZ();

        // Solid foundation & 13x13 stone-brick courtyard
        for (int x = ox - 6; x <= ox + 6; x++) {
            for (int z = oz - 6; z <= oz + 6; z++) {
                StructureBuildUtil.fillFoundationSolid(level, box, new BlockPos(x, oy, z), 6, Blocks.COBBLESTONE.defaultBlockState());
                boolean border = (Math.abs(x - ox) == 6 || Math.abs(z - oz) == 6);
                boolean cross = (x == ox || z == oz);
                StructureBuildUtil.setIfInside(level, box, new BlockPos(x, oy, z),
                        (border || cross) ? Blocks.CHISELED_STONE_BRICKS.defaultBlockState()
                                : ((x + z) % 2 == 0 ? Blocks.STONE_BRICKS.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState()));
            }
        }
        StructureBuildUtil.fillBox(level, box, ox - 6, oy + 1, oz - 6, ox + 6, oy + 7, oz + 6, Blocks.AIR.defaultBlockState());

        // 4 Corner oak-log pillars & wooden pavilion roof beams
        int[][] corners = {{-6, -6}, {6, -6}, {-6, 6}, {6, 6}};
        for (int[] c : corners) {
            for (int y = oy + 1; y <= oy + 5; y++) {
                StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + c[0], y, oz + c[1]), Blocks.OAK_LOG.defaultBlockState());
            }
            StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + c[0], oy + 6, oz + c[1]), Blocks.TORCH.defaultBlockState());
        }
        // Perimeter oak fence + entrance archways at N/S/E/W
        for (int i = -5; i <= 5; i++) {
            if (Math.abs(i) > 1) {
                StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + i, oy + 1, oz - 6), Blocks.OAK_FENCE.defaultBlockState());
                StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + i, oy + 1, oz + 6), Blocks.OAK_FENCE.defaultBlockState());
                StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 6, oy + 1, oz + i), Blocks.OAK_FENCE.defaultBlockState());
                StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 6, oy + 1, oz + i), Blocks.OAK_FENCE.defaultBlockState());
            }
        }
        // Open timber roof pergola border + glowstone lanterns
        for (int i = -6; i <= 6; i++) {
            StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + i, oy + 5, oz - 6), Blocks.OAK_PLANKS.defaultBlockState());
            StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + i, oy + 5, oz + 6), Blocks.OAK_PLANKS.defaultBlockState());
            StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 6, oy + 5, oz + i), Blocks.OAK_PLANKS.defaultBlockState());
            StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 6, oy + 5, oz + i), Blocks.OAK_PLANKS.defaultBlockState());
        }

        // Central mini cobblestone & glowstone fountain
        StructureBuildUtil.fillBox(level, box, ox - 1, oy + 1, oz - 1, ox + 1, oy + 1, oz + 1, Blocks.STONE_BRICKS.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox, oy + 1, oz), Blocks.GLOWSTONE.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox, oy + 2, oz), Blocks.COBBLESTONE_WALL.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox, oy + 3, oz), Blocks.WATER.defaultBlockState());

        // Rules & Staff Info Board on the North side (oz - 4)
        StructureBuildUtil.fillBox(level, box, ox - 2, oy + 1, oz - 4, ox + 2, oy + 3, oz - 4, Blocks.OAK_PLANKS.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 2, oy + 4, oz - 4), Blocks.TORCH.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 2, oy + 4, oz - 4), Blocks.REDSTONE_TORCH.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 2, oy + 2, oz - 4), ModBlocks.GLITCH_BLOCK.get().defaultBlockState());

        StructureBuildUtil.placeWallSign(level, box, new BlockPos(ox - 1, oy + 2, oz - 3), Direction.SOUTH,
                Blocks.OAK_WALL_SIGN, DyeColor.WHITE, false,
                "sign.under_world.spawn_rules.1", "sign.under_world.spawn_rules.2",
                "sign.under_world.spawn_rules.3", "sign.under_world.spawn_rules.4");
        StructureBuildUtil.placeWallSign(level, box, new BlockPos(ox + 1, oy + 2, oz - 3), Direction.SOUTH,
                Blocks.OAK_WALL_SIGN, DyeColor.RED, true,
                "sign.under_world.spawn_staff.1", "sign.under_world.spawn_staff.2",
                "sign.under_world.spawn_staff.3", "sign.under_world.spawn_staff.4");

        // Starter food chest + rulebook lectern
        ItemStack rulebook = StructureBuildUtil.createLoreBook("spawn_hub", "Server Rules [OLD]", "Moderator_Leo", 3);
        StructureBuildUtil.placeLecternWithBook(level, box, new BlockPos(ox, oy + 1, oz - 3), Direction.SOUTH, rulebook);
        StructureBuildUtil.placeChestWithItems(level, box, new BlockPos(ox + 4, oy + 1, oz - 4), Direction.SOUTH, random, null,
                new ItemStack(Items.BREAD, 6),
                new ItemStack(Items.APPLE, 4),
                new ItemStack(Items.WOODEN_PICKAXE, 1),
                new ItemStack(Items.TORCH, 8));
    }
}
