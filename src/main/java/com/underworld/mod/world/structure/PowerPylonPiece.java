package com.underworld.mod.world.structure;

import com.underworld.mod.registry.ModBlocks;
import com.underworld.mod.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
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

public class PowerPylonPiece extends StructurePiece {
    private final BlockPos origin;

    public PowerPylonPiece(BlockPos origin) {
        super(ModStructures.POWER_PYLON_PIECE.get(), 0,
                new BoundingBox(origin.getX() - 6, origin.getY() - 2, origin.getZ() - 6,
                        origin.getX() + 6, origin.getY() + 20, origin.getZ() + 6));
        this.origin = origin;
    }

    public PowerPylonPiece(CompoundTag tag) {
        super(ModStructures.POWER_PYLON_PIECE.get(), tag);
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
        build(level, box, this.origin);
    }

    public static void placeAt(ServerLevel level, BlockPos origin) {
        BoundingBox box = new BoundingBox(
                origin.getX() - 6, origin.getY() - 2, origin.getZ() - 6,
                origin.getX() + 6, origin.getY() + 20, origin.getZ() + 6
        );
        build(level, box, origin);
    }

    private static void build(ServerLevelAccessor level, BoundingBox box, BlockPos origin) {
        BlockState steel = ModBlocks.PYLON_STEEL.get().defaultBlockState();
        BlockState portalGrass = ModBlocks.PLAINS_PORTAL_GRASS.get().defaultBlockState();

        // Electrified dark portal turf underneath the Overworld power pylon!
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                setIfInside(level, box, origin.offset(dx, -1, dz), Blocks.DIRT.defaultBlockState());
                if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                    setIfInside(level, box, origin.offset(dx, 0, dz), portalGrass);
                } else {
                    setIfInside(level, box, origin.offset(dx, 0, dz), ModBlocks.PLAINS_GRASS_BLOCK.get().defaultBlockState());
                }
                for (int dy = 1; dy <= 5; dy++) {
                    setIfInside(level, box, origin.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
                }
            }
        }

        // 4 splayed legs y=0..6
        for (int dy = 0; dy <= 6; dy++) {
            int r = dy < 3 ? 3 : 2;
            setIfInside(level, box, origin.offset(-r, dy, -r), steel);
            setIfInside(level, box, origin.offset(r, dy, -r), steel);
            setIfInside(level, box, origin.offset(-r, dy, r), steel);
            setIfInside(level, box, origin.offset(r, dy, r), steel);
        }
        // Central mast y=7..18
        for (int dy = 7; dy <= 18; dy++) {
            setIfInside(level, box, origin.offset(0, dy, 0), steel);
            if (dy % 3 == 0) {
                setIfInside(level, box, origin.offset(1, dy, 0), Blocks.IRON_BARS.defaultBlockState());
                setIfInside(level, box, origin.offset(-1, dy, 0), Blocks.IRON_BARS.defaultBlockState());
            }
        }
        // Two cross-arms + dangling torn cable chains
        for (int dx = -5; dx <= 5; dx++) {
            setIfInside(level, box, origin.offset(dx, 13, 0), steel);
        }
        for (int dx = -4; dx <= 4; dx++) {
            setIfInside(level, box, origin.offset(dx, 17, 0), steel);
        }
        setIfInside(level, box, origin.offset(-5, 12, 0), Blocks.CHAIN.defaultBlockState());
        setIfInside(level, box, origin.offset(-5, 11, 0), Blocks.CHAIN.defaultBlockState());
        setIfInside(level, box, origin.offset(5, 12, 0), Blocks.CHAIN.defaultBlockState());
        setIfInside(level, box, origin.offset(5, 11, 0), Blocks.CHAIN.defaultBlockState());
        setIfInside(level, box, origin.offset(0, 19, 0), Blocks.REDSTONE_TORCH.defaultBlockState());

        StructureBuildUtil.placeWallOrStandingSign(level, box, origin.offset(0, 1, -3), Direction.NORTH, DyeColor.RED, true,
                "sign.under_world.power_pylon.1", "sign.under_world.power_pylon.2", "", "");
    }

    private static void setIfInside(ServerLevelAccessor level, BoundingBox box, BlockPos pos, BlockState state) {
        if (box.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }
}
