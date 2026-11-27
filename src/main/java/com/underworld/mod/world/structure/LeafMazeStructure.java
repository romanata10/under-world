package com.underworld.mod.world.structure;

import com.mojang.serialization.Codec;
import com.underworld.mod.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

public class LeafMazeStructure extends Structure {
    public static final Codec<LeafMazeStructure> CODEC = simpleCodec(LeafMazeStructure::new);

    public LeafMazeStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        return onTopOfChunkCenter(context, Heightmap.Types.WORLD_SURFACE_WG, builder -> {
            int cx = context.chunkPos().getMiddleBlockX();
            int cz = context.chunkPos().getMiddleBlockZ();
            int y = context.chunkGenerator().getFirstOccupiedHeight(cx, cz, Heightmap.Types.WORLD_SURFACE_WG,
                    context.heightAccessor(), context.randomState());
            if (y >= context.chunkGenerator().getSeaLevel()) {
                builder.addPiece(new LeafMazePiece(new BlockPos(cx, y - 1, cz)));
            }
        });
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.LEAF_MAZE.get();
    }
}
