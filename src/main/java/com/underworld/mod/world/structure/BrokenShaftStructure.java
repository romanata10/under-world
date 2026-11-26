package com.underworld.mod.world.structure;

import com.mojang.serialization.Codec;
import com.underworld.mod.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

public class BrokenShaftStructure extends Structure {
    public static final Codec<BrokenShaftStructure> CODEC = simpleCodec(BrokenShaftStructure::new);

    public BrokenShaftStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int x = context.chunkPos().getMiddleBlockX();
        int z = context.chunkPos().getMiddleBlockZ();
        int y = -16 + context.random().nextInt(32);
        BlockPos origin = new BlockPos(x, y, z);
        return Optional.of(new GenerationStub(origin, builder -> builder.addPiece(new BrokenShaftPiece(origin))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.BROKEN_SHAFT.get();
    }
}
