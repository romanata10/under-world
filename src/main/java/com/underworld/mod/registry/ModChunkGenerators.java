package com.underworld.mod.registry;

import com.mojang.serialization.Codec;
import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.world.dimension.FunWorldChunkGenerator;
import com.underworld.mod.world.dimension.MazeChunkGenerator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModChunkGenerators {
    public static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, UnderWorldMod.MOD_ID);

    public static final RegistryObject<Codec<FunWorldChunkGenerator>> THE_FUN_WORLD =
            CHUNK_GENERATORS.register("the_fun_world", () -> FunWorldChunkGenerator.CODEC);

    public static final RegistryObject<Codec<MazeChunkGenerator>> MAZE =
            CHUNK_GENERATORS.register("maze", () -> MazeChunkGenerator.CODEC);

    private ModChunkGenerators() {}
}
