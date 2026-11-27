package com.underworld.mod.world.structure;

import com.underworld.mod.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class OldVillageHousePiece extends StructurePiece {
    private final BlockPos origin;

    public OldVillageHousePiece(BlockPos origin) {
        super(ModStructures.OLD_VILLAGE_HOUSE_PIECE.get(), 0,
                new BoundingBox(origin.getX() - 5, origin.getY() - 6, origin.getZ() - 5,
                        origin.getX() + 5, origin.getY() + 9, origin.getZ() + 6));
        this.origin = origin;
    }

    public OldVillageHousePiece(CompoundTag tag) {
        super(ModStructures.OLD_VILLAGE_HOUSE_PIECE.get(), tag);
        this.origin = new BlockPos(tag.getInt("OX"), tag.getInt("OY"), tag.getInt("OZ"));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("OX", this.origin.getX());
        tag.putInt("OY", this.origin.getY());
        tag.putInt("OZ", this.origin.getZ());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
        build(level, random, box, this.origin);
    }

    public static void placeAt(ServerLevel level, BlockPos origin) {
        BoundingBox box = new BoundingBox(
                origin.getX() - 5, origin.getY() - 6, origin.getZ() - 5,
                origin.getX() + 5, origin.getY() + 9, origin.getZ() + 6
        );
        build(level, level.getRandom(), box, origin);
    }

    private static void build(ServerLevelAccessor level, RandomSource random, BoundingBox box, BlockPos origin) {
        BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
        BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState glass = Blocks.GLASS_PANE.defaultBlockState();

        // 1. Classic 1.12.2 cobblestone foundation & cleared interior
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                BlockPos floorPos = origin.offset(dx, 0, dz);
                StructureBuildUtil.setIfInside(level, box, floorPos, cobble);
                StructureBuildUtil.fillColumnDown(level, box, floorPos.below(), 6, cobble);
                for (int dy = 1; dy <= 7; dy++) {
                    StructureBuildUtil.setIfInside(level, box, origin.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
                }
            }
        }

        // 2. Classic 1.12.2 walls: cobble base + corners, oak planks, glass panes, wooden door
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                boolean wallX = Math.abs(dx) == 3;
                boolean wallZ = Math.abs(dz) == 3;
                if (!wallX && !wallZ) continue;
                boolean corner = wallX && wallZ;

                for (int dy = 1; dy <= 3; dy++) {
                    // Door opening at (0, 1..2, 3)
                    if (dx == 0 && dz == 3 && dy <= 2) continue;

                    // Classic side & back glass pane windows at dy == 2
                    boolean window = dy == 2 && !corner && (
                            (wallX && Math.abs(dz) <= 1) ||
                            (dz == -3 && Math.abs(dx) == 1) ||
                            (dz == 3 && Math.abs(dx) == 2)
                    );

                    BlockState block;
                    if (window) {
                        block = glass;
                    } else if (dy == 1 || corner) {
                        block = cobble;
                    } else {
                        block = planks;
                    }
                    StructureBuildUtil.setIfInside(level, box, origin.offset(dx, dy, dz), block);
                }

                // Top wooden log/plank wall ring at dy == 4
                StructureBuildUtil.setIfInside(level, box, origin.offset(dx, 4, dz), corner ? log : planks);
            }
        }

        // 3. Pitched 1.12.2 oak stair roof (gable facing N/S along Z)
        BlockState eastStair = Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST);
        BlockState westStair = Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST);
        for (int dz = -4; dz <= 4; dz++) {
            StructureBuildUtil.setIfInside(level, box, origin.offset(-4, 4, dz), eastStair);
            StructureBuildUtil.setIfInside(level, box, origin.offset(4, 4, dz), westStair);
            StructureBuildUtil.setIfInside(level, box, origin.offset(-3, 5, dz), eastStair);
            StructureBuildUtil.setIfInside(level, box, origin.offset(3, 5, dz), westStair);
            StructureBuildUtil.setIfInside(level, box, origin.offset(-2, 6, dz), eastStair);
            StructureBuildUtil.setIfInside(level, box, origin.offset(2, 6, dz), westStair);
            for (int dx = -1; dx <= 1; dx++) {
                StructureBuildUtil.setIfInside(level, box, origin.offset(dx, 7, dz), planks);
            }
        }
        // Gable plank infill on front & back walls
        for (int dz : new int[]{-3, 3}) {
            for (int dx = -2; dx <= 2; dx++) {
                StructureBuildUtil.setIfInside(level, box, origin.offset(dx, 5, dz), planks);
            }
            for (int dx = -1; dx <= 1; dx++) {
                StructureBuildUtil.setIfInside(level, box, origin.offset(dx, 6, dz), planks);
            }
        }

        // 4. Front step & oak door
        StructureBuildUtil.setIfInside(level, box, origin.offset(0, 0, 4),
                Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
        StructureBuildUtil.fillColumnDown(level, box, origin.offset(0, -1, 4), 5, cobble);
        BlockState lowerDoor = Blocks.OAK_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, Direction.NORTH)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
        BlockState upperDoor = Blocks.OAK_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, Direction.NORTH)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
        StructureBuildUtil.setIfInside(level, box, origin.offset(0, 1, 3), lowerDoor);
        StructureBuildUtil.setIfInside(level, box, origin.offset(0, 2, 3), upperDoor);

        // Exterior & interior vintage wall torches
        StructureBuildUtil.setIfInside(level, box, origin.offset(0, 3, 4),
                Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.SOUTH));
        StructureBuildUtil.setIfInside(level, box, origin.offset(0, 3, -2),
                Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.SOUTH));

        // 5. Iconic 1.12.2 villager cottage interior (fence + pressure plate table + benches + bookshelf + chest)
        StructureBuildUtil.setIfInside(level, box, origin.offset(-2, 1, -2), Blocks.OAK_FENCE.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, origin.offset(-2, 2, -2), Blocks.OAK_PRESSURE_PLATE.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, origin.offset(-1, 1, -2),
                Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST));
        StructureBuildUtil.setIfInside(level, box, origin.offset(-2, 1, -1),
                Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));

        StructureBuildUtil.setIfInside(level, box, origin.offset(2, 1, -2), Blocks.BOOKSHELF.defaultBlockState());
        StructureBuildUtil.setIfInside(level, box, origin.offset(2, 1, -1), Blocks.CRAFTING_TABLE.defaultBlockState());
        StructureBuildUtil.placeChest(level, box, origin.offset(2, 1, 1), random, BuiltInLootTables.VILLAGE_PLAINS_HOUSE);
    }
}
