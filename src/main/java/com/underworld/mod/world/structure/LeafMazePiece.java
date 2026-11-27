package com.underworld.mod.world.structure;

import com.underworld.mod.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class LeafMazePiece extends StructurePiece {
    // 15x15 crisp guaranteed-solvable oak leaf hedge maze!
    private static final String[] LAYOUT = {
            "###############",
            "#...#.....#...#",
            "#.#.#.###.#.#.#",
            "#.#...#...#.#.#",
            "#.#####.###.#.#",
            "#.....#.....#.#",
            "###.#.#.###.#.#",
            "#...#..C..#...#",
            "#.###.###.#.###",
            "#.#...#...#...#",
            "#.#.###.#####.#",
            "#.#.....#.....#",
            "#.#######.###.#",
            "#.........#...#",
            "#######.#######"
    };

    private final BlockPos origin;

    public LeafMazePiece(BlockPos origin) {
        super(ModStructures.LEAF_MAZE_PIECE.get(), 0,
                new BoundingBox(origin.getX() - 8, origin.getY() - 5, origin.getZ() - 8,
                        origin.getX() + 8, origin.getY() + 6, origin.getZ() + 8));
        this.origin = origin;
    }

    public LeafMazePiece(CompoundTag tag) {
        super(ModStructures.LEAF_MAZE_PIECE.get(), tag);
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
                origin.getX() - 8, origin.getY() - 5, origin.getZ() - 8,
                origin.getX() + 8, origin.getY() + 6, origin.getZ() + 8
        );
        build(level, level.getRandom(), box, origin);
    }

    private static void build(ServerLevelAccessor level, RandomSource random, BoundingBox box, BlockPos origin) {
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        BlockState mossy = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        BlockState path = Blocks.COARSE_DIRT.defaultBlockState();
        BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();

        for (int row = 0; row < 15; row++) {
            String line = LAYOUT[row];
            for (int col = 0; col < 15; col++) {
                int dx = col - 7;
                int dz = row - 7;
                char ch = line.charAt(col);

                BlockPos floorPos = origin.offset(dx, 0, dz);
                StructureBuildUtil.fillColumnDown(level, box, floorPos.below(), 5, Blocks.DIRT.defaultBlockState());
                for (int dy = 1; dy <= 5; dy++) {
                    StructureBuildUtil.setIfInside(level, box, origin.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
                }

                if (ch == '#') {
                    StructureBuildUtil.setIfInside(level, box, floorPos, random.nextFloat() < 0.35F ? mossy : grass);
                    for (int dy = 1; dy <= 3; dy++) {
                        StructureBuildUtil.setIfInside(level, box, origin.offset(dx, dy, dz), leaves);
                    }
                } else if (ch == 'C') {
                    StructureBuildUtil.setIfInside(level, box, floorPos, mossy);
                    StructureBuildUtil.placeChest(level, box, origin.offset(dx, 1, dz), random, BuiltInLootTables.SIMPLE_DUNGEON);
                    StructureBuildUtil.setIfInside(level, box, origin.offset(dx, 2, dz), Blocks.LANTERN.defaultBlockState());
                } else {
                    BlockState walkway = random.nextFloat() < 0.25F ? Blocks.GRAVEL.defaultBlockState() : path;
                    StructureBuildUtil.setIfInside(level, box, floorPos, walkway);
                }
            }
        }

        // Leaf archway frame over the South entrance (row 14, col 7 -> dx=0, dz=7)
        StructureBuildUtil.setIfInside(level, box, origin.offset(-1, 4, 7), leaves);
        StructureBuildUtil.setIfInside(level, box, origin.offset(0, 4, 7), leaves);
        StructureBuildUtil.setIfInside(level, box, origin.offset(1, 4, 7), leaves);
        StructureBuildUtil.setIfInside(level, box, origin.offset(0, 3, 7), Blocks.LANTERN.defaultBlockState());
    }
}
