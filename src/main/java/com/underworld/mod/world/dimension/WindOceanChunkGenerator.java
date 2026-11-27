package com.underworld.mod.world.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.underworld.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class WindOceanChunkGenerator extends ChunkGenerator {
    public static final int SEA_LEVEL = 63;
    public static final int ABYSS_FLOOR = -52;

    public static final Codec<WindOceanChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
            ).apply(instance, WindOceanChunkGenerator::new)
    );

    public WindOceanChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion level, long seed, RandomState random, BiomeManager biomeManager,
                             StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {
    }

    @Override
    public void buildSurface(WorldGenRegion level, StructureManager structureManager, RandomState random, ChunkAccess chunk) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {
    }

    @Override
    public int getGenDepth() {
        return 384;
    }

    @Override
    public int getSeaLevel() {
        return SEA_LEVEL;
    }

    @Override
    public int getMinY() {
        return -64;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState random,
                                                        StructureManager structureManager, ChunkAccess chunk) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        BlockState abyss = Blocks.BLACK_CONCRETE.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                pos.set(lx, -64, lz);
                chunk.setBlockState(pos, bedrock, false);
                for (int y = -63; y <= ABYSS_FLOOR; y++) {
                    pos.set(lx, y, lz);
                    chunk.setBlockState(pos, abyss, false);
                }
                for (int y = ABYSS_FLOOR + 1; y <= SEA_LEVEL; y++) {
                    pos.set(lx, y, lz);
                    chunk.setBlockState(pos, water, false);
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
        int cx = chunk.getPos().x;
        int cz = chunk.getPos().z;
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        // Oil Rig landmark at (144, 0) plus repeating offshore rigs every 32 chunks
        if (Math.floorMod(cx, 32) == 9 && Math.floorMod(cz, 32) == 0) {
            placeOilRig(level, new BlockPos(minX + 8, SEA_LEVEL, minZ + 8));
            return;
        }

        // Do not obstruct spawn boat landing (chunk 0, 0)
        if (cx == 0 && cz == 0) {
            return;
        }

        // Regular grid of towering offshore wind turbines matching image-16.png
        if (Math.floorMod(cx, 3) == 1 && Math.floorMod(cz, 3) == 1) {
            placeOffshoreTurbine(level, new BlockPos(minX + 8, SEA_LEVEL, minZ + 8), cx, cz);
        }
    }

    private static void placeOffshoreTurbine(WorldGenLevel level, BlockPos waterCenter, int cx, int cz) {
        BlockState pillar = ModBlocks.TURBINE_PILLAR.get().defaultBlockState();
        BlockState blade = ModBlocks.TURBINE_BLADE.get().defaultBlockState();
        // Underwater concrete piling y=50..64
        for (int dy = -12; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    setSafe(level, waterCenter.offset(dx, dy, dz), dy <= 0 ? Blocks.GRAY_CONCRETE.defaultBlockState() : pillar);
                }
            }
        }
        // Main shaft rising high above ocean surface y=65..92
        for (int dy = 2; dy <= 28; dy++) {
            setSafe(level, waterCenter.offset(0, dy, 0), pillar);
            if (dy < 16) {
                setSafe(level, waterCenter.offset(1, dy, 0), pillar);
                setSafe(level, waterCenter.offset(-1, dy, 0), pillar);
                setSafe(level, waterCenter.offset(0, dy, 1), pillar);
                setSafe(level, waterCenter.offset(0, dy, -1), pillar);
            }
        }
        // Nacelle + rotating angle silhouette depending on chunk
        for (int dz = -2; dz <= 3; dz++) {
            setSafe(level, waterCenter.offset(0, 29, dz), pillar);
            setSafe(level, waterCenter.offset(0, 30, dz), pillar);
        }
        boolean tilt = ((cx + cz) & 1) == 0;
        for (int i = 1; i <= 14; i++) {
            if (tilt) {
                setSafe(level, waterCenter.offset(0, 30 + i, -3), blade);
                setSafe(level, waterCenter.offset(i, 30 - i / 2, -3), blade);
                setSafe(level, waterCenter.offset(-i, 30 - i / 2, -3), blade);
            } else {
                setSafe(level, waterCenter.offset(i, 30 + i / 2, -3), blade);
                setSafe(level, waterCenter.offset(-i, 30 + i / 2, -3), blade);
                setSafe(level, waterCenter.offset(0, 30 - i, -3), blade);
            }
        }
        setSafe(level, waterCenter.offset(0, 31, 3), Blocks.REDSTONE_TORCH.defaultBlockState());
    }

    private static void placeOilRig(WorldGenLevel level, BlockPos base) {
        BlockState steel = ModBlocks.OIL_RIG_STEEL.get().defaultBlockState();
        BlockState dark = Blocks.DEEPSLATE_TILES.defaultBlockState();

        // 4 massive rig legs reaching deep underwater y=38..71
        int[][] legs = {{-6, -6}, {6, -6}, {-6, 6}, {6, 6}};
        for (int[] leg : legs) {
            for (int dy = -24; dy <= 8; dy++) {
                for (int ox = 0; ox <= 1; ox++) {
                    for (int oz = 0; oz <= 1; oz++) {
                        setSafe(level, base.offset(leg[0] + ox, dy, leg[1] + oz), steel);
                    }
                }
            }
        }
        // Boarding ladder pier on West (-X) face at water level
        for (int dy = 0; dy <= 8; dy++) {
            setSafe(level, base.offset(-7, dy, 0), Blocks.LADDER.defaultBlockState());
            setSafe(level, base.offset(-8, 1, 0), steel);
        }

        // Main steel deck platform at y=71 (base+8)
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                setSafe(level, base.offset(dx, 8, dz), (Math.abs(dx) <= 2 && Math.abs(dz) <= 2) ? dark : steel);
                if (Math.abs(dx) == 8 || Math.abs(dz) == 8) {
                    setSafe(level, base.offset(dx, 9, dz), Blocks.IRON_BARS.defaultBlockState());
                }
            }
        }

        // Control cabin & helipad portal
        for (int dx = 3; dx <= 7; dx++) {
            for (int dz = -5; dz <= -1; dz++) {
                for (int dy = 9; dy <= 13; dy++) {
                    boolean wall = dx == 3 || dx == 7 || dz == -5 || dz == -1 || dy == 13;
                    setSafe(level, base.offset(dx, dy, dz), wall ? steel : Blocks.AIR.defaultBlockState());
                }
            }
        }
        setSafe(level, base.offset(3, 10, -3), Blocks.AIR.defaultBlockState());
        setSafe(level, base.offset(3, 11, -3), Blocks.AIR.defaultBlockState());
        setSafe(level, base.offset(2, 11, -3), Blocks.LANTERN.defaultBlockState());

        // Tall derrick mast & visible burning flare beacon
        for (int dy = 9; dy <= 28; dy++) {
            setSafe(level, base.offset(-5, dy, 5), steel);
            setSafe(level, base.offset(-4, dy, 5), steel);
        }
        setSafe(level, base.offset(-5, 29, 5), Blocks.NETHERRACK.defaultBlockState());
        setSafe(level, base.offset(-5, 30, 5), Blocks.FIRE.defaultBlockState());
        setSafe(level, base.offset(-4, 29, 5), Blocks.LANTERN.defaultBlockState());

        // Glowing Oil Rig return portal at center of main deck!
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                setSafe(level, base.offset(dx, 9, dz), ModBlocks.OIL_RIG_PORTAL.get().defaultBlockState());
            }
        }
    }

    private static void setSafe(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (pos.getY() >= level.getMinBuildHeight() && pos.getY() < level.getMaxBuildHeight()) {
            level.setBlock(pos, state, 2);
        }
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        return SEA_LEVEL + 1;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
        int height = level.getHeight();
        BlockState[] states = new BlockState[height];
        int minY = level.getMinBuildHeight();
        for (int i = 0; i < height; i++) {
            int y = minY + i;
            if (y == -64) states[i] = Blocks.BEDROCK.defaultBlockState();
            else if (y <= ABYSS_FLOOR) states[i] = Blocks.BLACK_CONCRETE.defaultBlockState();
            else if (y <= SEA_LEVEL) states[i] = Blocks.WATER.defaultBlockState();
            else states[i] = Blocks.AIR.defaultBlockState();
        }
        return new NoiseColumn(minY, states);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        info.add("Under World: The Wind Ocean");
    }
}
