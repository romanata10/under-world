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
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class BrokenShaftPiece extends StructurePiece {
    private final BlockPos origin;

    public BrokenShaftPiece(BlockPos origin) {
        super(ModStructures.BROKEN_SHAFT_PIECE.get(), 0,
                new BoundingBox(origin.getX() - 4, origin.getY() - 1, origin.getZ() - 7,
                        origin.getX() + 4, origin.getY() + 6, origin.getZ() + 7));
        this.origin = origin;
    }

    public BrokenShaftPiece(CompoundTag tag) {
        super(ModStructures.BROKEN_SHAFT_PIECE.get(), tag);
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
                origin.getX() - 4, origin.getY() - 1, origin.getZ() - 7,
                origin.getX() + 4, origin.getY() + 6, origin.getZ() + 7
        );
        build(level, level.getRandom(), fullBox, origin);
    }

    private static void build(ServerLevelAccessor level, RandomSource random, BoundingBox clipBox, BlockPos origin) {
        // Carve void-corrupted underground shaft (7x5x13)
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
                boolean isBorderX = Math.abs(dx) == 3;
                boolean isBorderZ = Math.abs(dz) == 6;

                BlockState floor = random.nextFloat() < 0.2F
                        ? Blocks.BLACK_CONCRETE.defaultBlockState()
                        : (random.nextFloat() < 0.45F ? Blocks.DEEPSLATE_BRICKS.defaultBlockState() : Blocks.COBBLED_DEEPSLATE.defaultBlockState());
                setIfInside(level, clipBox, origin.offset(dx, 0, dz), floor);

                for (int dy = 1; dy <= 4; dy++) {
                    if (isBorderX || isBorderZ) {
                        if (random.nextFloat() < 0.72F) {
                            BlockState wall = random.nextFloat() < 0.18F
                                    ? Blocks.BLACK_CONCRETE.defaultBlockState()
                                    : Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
                            setIfInside(level, clipBox, origin.offset(dx, dy, dz), wall);
                        } else {
                            setIfInside(level, clipBox, origin.offset(dx, dy, dz), Blocks.CAVE_AIR.defaultBlockState());
                        }
                    } else {
                        setIfInside(level, clipBox, origin.offset(dx, dy, dz), Blocks.CAVE_AIR.defaultBlockState());
                    }
                }

                BlockState ceiling = random.nextFloat() < 0.25F
                        ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
                        : Blocks.DEEPSLATE_BRICKS.defaultBlockState();
                setIfInside(level, clipBox, origin.offset(dx, 5, dz), ceiling);
            }
        }

        // Oak support arches, hanging chains, and cobwebs
        for (int dz = -4; dz <= 4; dz += 4) {
            for (int dy = 1; dy <= 3; dy++) {
                setIfInside(level, clipBox, origin.offset(-2, dy, dz), Blocks.OAK_FENCE.defaultBlockState());
                setIfInside(level, clipBox, origin.offset(2, dy, dz), Blocks.OAK_FENCE.defaultBlockState());
            }
            for (int dx = -2; dx <= 2; dx++) {
                setIfInside(level, clipBox, origin.offset(dx, 4, dz), Blocks.OAK_PLANKS.defaultBlockState());
            }
            setIfInside(level, clipBox, origin.offset(0, 3, dz), Blocks.CHAIN.defaultBlockState());
            if (dz == 0) {
                setIfInside(level, clipBox, origin.offset(0, 2, dz), Blocks.SOUL_LANTERN.defaultBlockState());
            }
        }

        setIfInside(level, clipBox, origin.offset(-2, 3, -2), Blocks.COBWEB.defaultBlockState());
        setIfInside(level, clipBox, origin.offset(2, 3, 2), Blocks.COBWEB.defaultBlockState());
        setIfInside(level, clipBox, origin.offset(0, 1, -5), Blocks.REDSTONE_TORCH.defaultBlockState());

        BlockPos chestPos = origin.offset(1, 1, 4);
        if (clipBox.isInside(chestPos)) {
            level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
            if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
                chest.setLootTable(BuiltInLootTables.ABANDONED_MINESHAFT, random.nextLong());
            }
        }
    }

    private static void setIfInside(ServerLevelAccessor level, BoundingBox box, BlockPos pos, BlockState state) {
        if (box.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }
}
