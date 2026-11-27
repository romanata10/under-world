package com.underworld.mod.world.structure.lore;

import com.underworld.mod.registry.ModBlocks;
import com.underworld.mod.world.structure.StructureBuildUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Cobbled & oak Villager Trading Hall with 3 working villager stalls,
 * eerie quarantine signs, emerald stash, and the trader's final logbook.
 */
public final class LoreTradingHallPiece {
    private LoreTradingHallPiece() {}

    public static void placeAt(ServerLevel level, BlockPos origin) {
        BoundingBox box = new BoundingBox(
                origin.getX() - 7, origin.getY() - 2, origin.getZ() - 5,
                origin.getX() + 7, origin.getY() + 7, origin.getZ() + 5
        );
        build(level, level.getRandom(), box, origin);
    }

    public static void build(ServerLevelAccessor level, RandomSource random, BoundingBox box, BlockPos origin) {
        int ox = origin.getX();
        int oy = origin.getY();
        int oz = origin.getZ();

        // Solidify ground beneath trading hall
        for (int x = ox - 6; x <= ox + 6; x++) {
            for (int z = oz - 4; z <= oz + 4; z++) {
                StructureBuildUtil.fillFoundationSolid(level, box, new BlockPos(x, oy, z), 6, Blocks.COBBLESTONE.defaultBlockState());
                StructureBuildUtil.setIfInside(level, box, new BlockPos(x, oy, z),
                        ((x + z) % 2 == 0) ? Blocks.STONE_BRICKS.defaultBlockState() : Blocks.OAK_PLANKS.defaultBlockState());
            }
        }
        StructureBuildUtil.fillBox(level, box, ox - 6, oy + 1, oz - 4, ox + 6, oy + 6, oz + 4, Blocks.AIR.defaultBlockState());

        // Perimeter walls + pitched oak roof
        StructureBuildUtil.fillBox(level, box, ox - 6, oy + 1, oz - 4, ox + 6, oy + 4, oz - 4, Blocks.COBBLESTONE.defaultBlockState());
        StructureBuildUtil.fillBox(level, box, ox - 6, oy + 1, oz + 4, ox + 6, oy + 4, oz + 4, Blocks.OAK_PLANKS.defaultBlockState());
        StructureBuildUtil.fillBox(level, box, ox - 6, oy + 1, oz - 4, ox - 6, oy + 4, oz + 4, Blocks.COBBLESTONE.defaultBlockState());
        StructureBuildUtil.fillBox(level, box, ox + 6, oy + 1, oz - 4, ox + 6, oy + 4, oz + 4, Blocks.COBBLESTONE.defaultBlockState());
        StructureBuildUtil.fillBox(level, box, ox - 6, oy + 5, oz - 4, ox + 6, oy + 5, oz + 4, Blocks.SPRUCE_PLANKS.defaultBlockState());

        // Front entrance doors at (ox, oz + 4) + torches
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox, oy + 1, oz + 4),
                Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH).setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox, oy + 2, oz + 4),
                Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH).setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 2, oy + 2, oz + 5), Blocks.TORCH.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 2, oy + 2, oz + 5), Blocks.TORCH.defaultBlockState());

        // 3 Villager stalls along back wall (x centers: ox-4, ox, ox+4; z: oz-3..oz-1)
        int[] stallX = {ox - 4, ox, ox + 4};
        VillagerProfession[] professions = {VillagerProfession.LIBRARIAN, VillagerProfession.ARMORER, VillagerProfession.FARMER};
        for (int idx = 0; idx < stallX.length; idx++) {
            int sx = stallX[idx];
            // Side stall dividers
            for (int z = oz - 3; z <= oz - 1; z++) {
                StructureBuildUtil.fillBox(level, box, sx - 2, oy + 1, z, sx - 2, oy + 3, z, Blocks.OAK_PLANKS.defaultBlockState());
                StructureBuildUtil.fillBox(level, box, sx + 2, oy + 1, z, sx + 2, oy + 3, z, Blocks.OAK_PLANKS.defaultBlockState());
            }
            // Front counter barrier: workstation + oak trapdoor/slab
            if (idx == 0) {
                StructureBuildUtil.setIfInside(level, box, new BlockPos(sx, oy + 1, oz - 1), Blocks.LECTERN.defaultBlockState());
            } else if (idx == 1) {
                StructureBuildUtil.setIfInside(level, box, new BlockPos(sx, oy + 1, oz - 1), Blocks.BLAST_FURNACE.defaultBlockState());
            } else {
                StructureBuildUtil.setIfInside(level, box, new BlockPos(sx, oy + 1, oz - 1), Blocks.COMPOSTER.defaultBlockState());
            }
            StructureBuildUtil.setIfInside(level, box, new BlockPos(sx - 1, oy + 1, oz - 1), Blocks.OAK_FENCE.defaultBlockState());
            StructureBuildUtil.setIfInside(level, box, new BlockPos(sx + 1, oy + 1, oz - 1),
                    Blocks.OAK_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING, Direction.SOUTH));
            StructureBuildUtil.setIfInside(level, box, new BlockPos(sx, oy + 4, oz - 2), Blocks.GLOWSTONE.defaultBlockState());

            // Spawn villager inside stall!
            if (box.isInside(new BlockPos(sx, oy + 1, oz - 2))) {
                Villager villager = EntityType.VILLAGER.create(level.getLevel());
                if (villager != null) {
                    villager.moveTo(sx + 0.5D, oy + 1.0D, oz - 2.0D + 0.5D, 0.0F, 0.0F);
                    villager.setVillagerData(villager.getVillagerData().setProfession(professions[idx]).setLevel(2));
                    villager.setPersistenceRequired();
                    level.addFreshEntity(villager);
                }
            }
        }

        // Anomaly & interior lights
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox - 5, oy + 2, oz - 4), ModBlocks.GLITCH_BLOCK.get().defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, new BlockPos(ox + 5, oy + 2, oz + 3), Blocks.REDSTONE_TORCH.defaultBlockState());

        // Trader's Emerald Chest + Logbook + Cryptic Sign
        ItemStack traderLog = StructureBuildUtil.createLoreBook("trading_hall", "Market Quarantine", "Trader_Vova", 3);
        StructureBuildUtil.placeChestWithItems(level, box, new BlockPos(ox - 4, oy + 1, oz + 3), Direction.NORTH, random, null,
                traderLog,
                new ItemStack(Items.EMERALD, 11),
                new ItemStack(Items.PAPER, 18),
                new ItemStack(Items.BREAD, 6));

        StructureBuildUtil.placeWallSign(level, box, new BlockPos(ox + 1, oy + 2, oz + 5), Direction.SOUTH,
                Blocks.OAK_WALL_SIGN, DyeColor.RED, false,
                "sign.under_world.trading.1", "sign.under_world.trading.2",
                "sign.under_world.trading.3", "sign.under_world.trading.4");
    }
}
