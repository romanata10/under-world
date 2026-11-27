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

        // Oil Rig landmark strictly checked per chunk around (144, 0) + every 32 chunks
        int rigSectorX = Math.floorDiv(cx, 32) * 32 + 9;
        int rigSectorZ = Math.floorDiv(cz, 32) * 32;
        if (Math.abs(cx - rigSectorX) <= 1 && Math.abs(cz - rigSectorZ) <= 1) {
            placeOilRigInChunk(chunk, minX, minZ, new BlockPos(rigSectorX * 16 + 8, SEA_LEVEL, rigSectorZ * 16 + 8));
        }

        // Do not obstruct spawn boat landing (chunk 0, 0)
        if (cx == 0 && cz == 0) {
            return;
        }

        // Regular grid of offshore wind turbines (check nearby turbine grid cell so blades clip cleanly at chunk edges!)
        for (int tcx = cx - 1; tcx <= cx + 1; tcx++) {
            for (int tcz = cz - 1; tcz <= cz + 1; tcz++) {
                if (Math.floorMod(tcx, 3) == 1 && Math.floorMod(tcz, 3) == 1
                        && !(Math.abs(tcx - rigSectorX) <= 1 && Math.abs(tcz - rigSectorZ) <= 1)) {
                    placeOffshoreTurbineInChunk(chunk, minX, minZ,
                            new BlockPos(tcx * 16 + 8, SEA_LEVEL, tcz * 16 + 8), tcx, tcz);
                }
            }
        }
    }

    private static void placeOffshoreTurbineInChunk(ChunkAccess chunk, int minX, int minZ, BlockPos waterCenter, int tcx, int tcz) {
        BlockState pillar = ModBlocks.TURBINE_PILLAR.get().defaultBlockState();
        BlockState blade = ModBlocks.TURBINE_BLADE.get().defaultBlockState();

        for (int dy = -12; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    setInChunk(chunk, minX, minZ, waterCenter.getX() + dx, waterCenter.getY() + dy, waterCenter.getZ() + dz,
                            dy <= 0 ? Blocks.GRAY_CONCRETE.defaultBlockState() : pillar);
                }
            }
        }
        for (int dy = 2; dy <= 28; dy++) {
            setInChunk(chunk, minX, minZ, waterCenter.getX(), waterCenter.getY() + dy, waterCenter.getZ(), pillar);
            if (dy < 16) {
                setInChunk(chunk, minX, minZ, waterCenter.getX() + 1, waterCenter.getY() + dy, waterCenter.getZ(), pillar);
                setInChunk(chunk, minX, minZ, waterCenter.getX() - 1, waterCenter.getY() + dy, waterCenter.getZ(), pillar);
                setInChunk(chunk, minX, minZ, waterCenter.getX(), waterCenter.getY() + dy, waterCenter.getZ() + 1, pillar);
                setInChunk(chunk, minX, minZ, waterCenter.getX(), waterCenter.getY() + dy, waterCenter.getZ() - 1, pillar);
            }
        }
        for (int dz = -2; dz <= 3; dz++) {
            setInChunk(chunk, minX, minZ, waterCenter.getX(), waterCenter.getY() + 29, waterCenter.getZ() + dz, pillar);
            setInChunk(chunk, minX, minZ, waterCenter.getX(), waterCenter.getY() + 30, waterCenter.getZ() + dz, pillar);
        }
        boolean tilt = ((tcx + tcz) & 1) == 0;
        for (int i = 1; i <= 14; i++) {
            if (tilt) {
                setInChunk(chunk, minX, minZ, waterCenter.getX(), waterCenter.getY() + 30 + i, waterCenter.getZ() - 3, blade);
                setInChunk(chunk, minX, minZ, waterCenter.getX() + i, waterCenter.getY() + 30 - i / 2, waterCenter.getZ() - 3, blade);
                setInChunk(chunk, minX, minZ, waterCenter.getX() - i, waterCenter.getY() + 30 - i / 2, waterCenter.getZ() - 3, blade);
            } else {
                setInChunk(chunk, minX, minZ, waterCenter.getX() + i, waterCenter.getY() + 30 + i / 2, waterCenter.getZ() - 3, blade);
                setInChunk(chunk, minX, minZ, waterCenter.getX() - i, waterCenter.getY() + 30 + i / 2, waterCenter.getZ() - 3, blade);
                setInChunk(chunk, minX, minZ, waterCenter.getX(), waterCenter.getY() + 30 - i, waterCenter.getZ() - 3, blade);
            }
        }
        setInChunk(chunk, minX, minZ, waterCenter.getX(), waterCenter.getY() + 31, waterCenter.getZ() + 3, Blocks.REDSTONE_TORCH.defaultBlockState());
    }

    private static void placeOilRigInChunk(ChunkAccess chunk, int minX, int minZ, BlockPos base) {
        BlockState steel = ModBlocks.OIL_RIG_STEEL.get().defaultBlockState();
        BlockState dark = Blocks.DEEPSLATE_TILES.defaultBlockState();

        int[][] legs = {{-6, -6}, {6, -6}, {-6, 6}, {6, 6}};
        for (int[] leg : legs) {
            for (int dy = -24; dy <= 8; dy++) {
                for (int ox = 0; ox <= 1; ox++) {
                    for (int oz = 0; oz <= 1; oz++) {
                        setInChunk(chunk, minX, minZ, base.getX() + leg[0] + ox, base.getY() + dy, base.getZ() + leg[1] + oz, steel);
                    }
                }
            }
        }
        for (int dy = 0; dy <= 8; dy++) {
            setInChunk(chunk, minX, minZ, base.getX() - 7, base.getY() + dy, base.getZ(), Blocks.LADDER.defaultBlockState());
            setInChunk(chunk, minX, minZ, base.getX() - 8, base.getY() + 1, base.getZ(), steel);
        }

        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                setInChunk(chunk, minX, minZ, base.getX() + dx, base.getY() + 8, base.getZ() + dz,
                        (Math.abs(dx) <= 2 && Math.abs(dz) <= 2) ? dark : steel);
                if (Math.abs(dx) == 8 || Math.abs(dz) == 8) {
                    setInChunk(chunk, minX, minZ, base.getX() + dx, base.getY() + 9, base.getZ() + dz, Blocks.IRON_BARS.defaultBlockState());
                }
            }
        }

        for (int dx = 3; dx <= 7; dx++) {
            for (int dz = -5; dz <= -1; dz++) {
                for (int dy = 9; dy <= 13; dy++) {
                    boolean wall = dx == 3 || dx == 7 || dz == -5 || dz == -1 || dy == 13;
                    setInChunk(chunk, minX, minZ, base.getX() + dx, base.getY() + dy, base.getZ() + dz,
                            wall ? steel : Blocks.AIR.defaultBlockState());
                }
            }
        }
        setInChunk(chunk, minX, minZ, base.getX() + 3, base.getY() + 10, base.getZ() - 3, Blocks.AIR.defaultBlockState());
        setInChunk(chunk, minX, minZ, base.getX() + 3, base.getY() + 11, base.getZ() - 3, Blocks.AIR.defaultBlockState());
        setInChunk(chunk, minX, minZ, base.getX() + 2, base.getY() + 11, base.getZ() - 3, Blocks.LANTERN.defaultBlockState());

        for (int dy = 9; dy <= 28; dy++) {
            setInChunk(chunk, minX, minZ, base.getX() - 5, base.getY() + dy, base.getZ() + 5, steel);
            setInChunk(chunk, minX, minZ, base.getX() - 4, base.getY() + dy, base.getZ() + 5, steel);
        }
        setInChunk(chunk, minX, minZ, base.getX() - 5, base.getY() + 29, base.getZ() + 5, Blocks.NETHERRACK.defaultBlockState());
        setInChunk(chunk, minX, minZ, base.getX() - 5, base.getY() + 30, base.getZ() + 5, Blocks.FIRE.defaultBlockState());
        setInChunk(chunk, minX, minZ, base.getX() - 4, base.getY() + 29, base.getZ() + 5, Blocks.LANTERN.defaultBlockState());

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                setInChunk(chunk, minX, minZ, base.getX() + dx, base.getY() + 9, base.getZ() + dz,
                        ModBlocks.OIL_RIG_PORTAL.get().defaultBlockState());
            }
        }
    }

    private static void setInChunk(ChunkAccess chunk, int minX, int minZ, int wx, int wy, int wz, BlockState state) {
        int lx = wx - minX;
        int lz = wz - minZ;
        if (lx >= 0 && lx < 16 && lz >= 0 && lz < 16 && wy >= -64 && wy < 320) {
            chunk.setBlockState(new BlockPos(lx, wy, lz), state, false);
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
