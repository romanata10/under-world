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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class VoidShrinePiece extends StructurePiece {
    private final BlockPos origin;

    public VoidShrinePiece(BlockPos origin) {
        super(ModStructures.VOID_SHRINE_PIECE.get(), 0,
                new BoundingBox(origin.getX() - 4, origin.getY() - 2, origin.getZ() - 4,
                        origin.getX() + 4, origin.getY() + 8, origin.getZ() + 4));
        this.origin = origin;
    }

    public VoidShrinePiece(CompoundTag tag) {
        super(ModStructures.VOID_SHRINE_PIECE.get(), tag);
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
        BoundingBox fullBox = new BoundingBox(
                origin.getX() - 4, origin.getY() - 2, origin.getZ() - 4,
                origin.getX() + 4, origin.getY() + 8, origin.getZ() + 4
        );
        build(level, level.getRandom(), fullBox, origin);
    }

    private static void build(ServerLevelAccessor level, RandomSource random, BoundingBox clipBox, BlockPos origin) {
        // Foundation and mossy/cracked blackstone ritual platform
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (Math.abs(dx) == 3 && Math.abs(dz) == 3) continue;
                BlockPos floorPos = origin.offset(dx, 0, dz);
                BlockState floorState = random.nextFloat() < 0.25F
                        ? Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                        : Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
                setIfInside(level, clipBox, floorPos, floorState);
                setIfInside(level, clipBox, floorPos.below(), Blocks.DEEPSLATE_TILES.defaultBlockState());

                for (int dy = 1; dy <= 6; dy++) {
                    setIfInside(level, clipBox, origin.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
                }
            }
        }

        // Four jagged obsidian/crying obsidian corner pillars
        int[][] corners = {{-2, -2}, {2, -2}, {-2, 2}, {2, 2}};
        for (int[] c : corners) {
            int height = 3 + random.nextInt(3);
            for (int dy = 1; dy <= height; dy++) {
                BlockState pillar = (dy == height || random.nextFloat() < 0.3F)
                        ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
                        : Blocks.OBSIDIAN.defaultBlockState();
                setIfInside(level, clipBox, origin.offset(c[0], dy, c[1]), pillar);
            }
            setIfInside(level, clipBox, origin.offset(c[0], height + 1, c[1]), Blocks.SOUL_LANTERN.defaultBlockState());
        }

        // Central void altar + chest
        setIfInside(level, clipBox, origin.offset(0, 1, 0), Blocks.CRYING_OBSIDIAN.defaultBlockState());
        setIfInside(level, clipBox, origin.offset(0, 2, 0), Blocks.BLACK_CONCRETE.defaultBlockState());
        setIfInside(level, clipBox, origin.offset(0, 3, 0), Blocks.REDSTONE_TORCH.defaultBlockState());

        BlockPos chestPos = origin.offset(0, 1, 1);
        if (clipBox.isInside(chestPos)) {
            level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
            if (level.getBlockEntity(chestPos) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest) {
                chest.setLootTable(BuiltInLootTables.SIMPLE_DUNGEON, random.nextLong());
            }
        }
    }

    private static void setIfInside(ServerLevelAccessor level, BoundingBox box, BlockPos pos, BlockState state) {
        if (box.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }
}
