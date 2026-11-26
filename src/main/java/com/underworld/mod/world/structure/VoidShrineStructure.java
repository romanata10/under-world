package com.underworld.mod.world.structure;

import com.mojang.serialization.Codec;
import com.underworld.mod.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

public class VoidShrineStructure extends Structure {
    public static final Codec<VoidShrineStructure> CODEC = simpleCodec(VoidShrineStructure::new);

    public VoidShrineStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        return onTopOfChunkCenter(context, Heightmap.Types.WORLD_SURFACE_WG, builder -> {
            int centerX = context.chunkPos().getMiddleBlockX();
            int centerZ = context.chunkPos().getMiddleBlockZ();
            int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(
                    centerX, centerZ, Heightmap.Types.WORLD_SURFACE_WG,
                    context.heightAccessor(), context.randomState());
            BlockPos origin = new BlockPos(centerX, surfaceY, centerZ);
            builder.addPiece(new VoidShrinePiece(origin));
        });
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.VOID_SHRINE.get();
    }
}
