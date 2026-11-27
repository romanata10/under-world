package com.underworld.mod.registry;

import com.mojang.serialization.Codec;
import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.world.dimension.BrowserChunkGenerator;
import com.underworld.mod.world.dimension.ErrorChunkGenerator;
import com.underworld.mod.world.dimension.FunWorldChunkGenerator;
import com.underworld.mod.world.dimension.MazeChunkGenerator;
import com.underworld.mod.world.dimension.PlainsChunkGenerator;
import com.underworld.mod.world.dimension.TimeChunkGenerator;
import com.underworld.mod.world.dimension.WindOceanChunkGenerator;
import com.underworld.mod.world.dimension.XpDesktopChunkGenerator;
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

    public static final RegistryObject<Codec<ErrorChunkGenerator>> THE_ERROR =
            CHUNK_GENERATORS.register("the_error", () -> ErrorChunkGenerator.CODEC);

    public static final RegistryObject<Codec<TimeChunkGenerator>> THE_TIME =
            CHUNK_GENERATORS.register("the_time", () -> TimeChunkGenerator.CODEC);

    public static final RegistryObject<Codec<PlainsChunkGenerator>> THE_PLAINS =
            CHUNK_GENERATORS.register("the_plains", () -> PlainsChunkGenerator.CODEC);

    public static final RegistryObject<Codec<WindOceanChunkGenerator>> THE_WIND_OCEAN =
            CHUNK_GENERATORS.register("the_wind_ocean", () -> WindOceanChunkGenerator.CODEC);

    public static final RegistryObject<Codec<XpDesktopChunkGenerator>> THE_XP =
            CHUNK_GENERATORS.register("the_xp", () -> XpDesktopChunkGenerator.CODEC);

    public static final RegistryObject<Codec<BrowserChunkGenerator>> THE_BROWSER =
            CHUNK_GENERATORS.register("the_browser", () -> BrowserChunkGenerator.CODEC);

    private ModChunkGenerators() {}
}
