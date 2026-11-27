package com.underworld.mod.world.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.underworld.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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

public class ErrorChunkGenerator extends ChunkGenerator {
    public static final Codec<ErrorChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
            ).apply(instance, ErrorChunkGenerator::new)
    );

    public static final int ANOMALY_NORMAL = 0;
    public static final int ANOMALY_VOID_HOLE = 1;
    public static final int ANOMALY_SKY_LIFTED = 2;
    public static final int ANOMALY_CORRUPT_STEP = 3;

    public ErrorChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    public static int getBaseSurfaceY(int wx, int wz) {
        double h1 = Math.sin(wx * 0.032D) * Math.cos(wz * 0.029D) * 7.5D;
        double h2 = Math.sin((wx - wz) * 0.017D) * 4.2D;
        double h3 = Math.cos(wx * 0.078D + wz * 0.062D) * 2.1D;
        return Mth.clamp((int) Math.round(72.0D + h1 + h2 + h3), 62, 92);
    }

    public static int getChunkAnomaly(int cx, int cz) {
        if (Math.abs(cx) <= 1 && Math.abs(cz) <= 1) {
            return ANOMALY_NORMAL;
        }
        long h = (cx * 73856093L) ^ (cz * 19349663L) ^ 0x40404040L;
        h ^= (h >>> 16);
        int mod = Math.floorMod((int) h, 100);
        if (mod < 14) {
            return ANOMALY_VOID_HOLE;
        }
        if (mod < 30) {
            return ANOMALY_SKY_LIFTED;
        }
        if (mod < 42) {
            return ANOMALY_CORRUPT_STEP;
        }
        return ANOMALY_NORMAL;
    }

    public static int getSkyLiftOffset(int cx, int cz) {
        long h = (cx * 2654435761L) ^ (cz * 2246822519L);
        return 34 + Math.floorMod((int) (h >>> 4), 32);
    }

    public static int getSurfaceYAt(int wx, int wz) {
        int cx = Math.floorDiv(wx, 16);
        int cz = Math.floorDiv(wz, 16);
        int anomaly = getChunkAnomaly(cx, cz);
        int base = getBaseSurfaceY(wx, wz);
        if (anomaly == ANOMALY_VOID_HOLE) {
            return -64;
        }
        if (anomaly == ANOMALY_SKY_LIFTED) {
            return Math.min(260, base + getSkyLiftOffset(cx, cz));
        }
        if (anomaly == ANOMALY_CORRUPT_STEP) {
            return base + (((wx & 3) == 0 || (wz & 3) == 0) ? 7 : -5);
        }
        return base;
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
        return 63;
    }

    @Override
    public int getMinY() {
        return -64;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState random,
                                                        StructureManager structureManager, ChunkAccess chunk) {
        int cx = chunk.getPos().x;
        int cz = chunk.getPos().z;
        int anomaly = getChunkAnomaly(cx, cz);

        if (anomaly == ANOMALY_VOID_HOLE) {
            // Leave an empty missing void chunk with occasional stray floating glitch shard
            if ((Math.floorMod(cx * 7 + cz * 13, 3)) == 0) {
                BlockState glitch = ModBlocks.GLITCH_BLOCK.get().defaultBlockState();
                chunk.setBlockState(new BlockPos(7, 74, 7), glitch, false);
                chunk.setBlockState(new BlockPos(8, 74, 7), ModBlocks.ERROR_GROUND.get().defaultBlockState(), false);
            }
            return CompletableFuture.completedFuture(chunk);
        }

        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        BlockState top = ModBlocks.ERROR_GROUND.get().defaultBlockState();
        BlockState sub = ModBlocks.ERROR_SOIL.get().defaultBlockState();
        BlockState glitch = ModBlocks.GLITCH_BLOCK.get().defaultBlockState();
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();

        if (anomaly == ANOMALY_SKY_LIFTED) {
            int lift = getSkyLiftOffset(cx, cz);
            for (int lx = 0; lx < 16; lx++) {
                for (int lz = 0; lz < 16; lz++) {
                    int base = getBaseSurfaceY(minX + lx, minZ + lz);
                    int chunkTop = Math.min(270, base + lift);
                    int chunkBottom = chunkTop - 11;
                    boolean edge = lx == 0 || lx == 15 || lz == 0 || lz == 15;
                    for (int y = chunkBottom; y < chunkTop; y++) {
                        pos.set(lx, y, lz);
                        if (edge && y == chunkBottom && ((lx + lz) % 5 == 0)) {
                            chunk.setBlockState(pos, glitch, false);
                        } else {
                            chunk.setBlockState(pos, sub, false);
                        }
                    }
                    pos.set(lx, chunkTop, lz);
                    chunk.setBlockState(pos, top, false);
                }
            }
            return CompletableFuture.completedFuture(chunk);
        }

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int wx = minX + lx;
                int wz = minZ + lz;
                int surface = getSurfaceYAt(wx, wz);

                pos.set(lx, -64, lz);
                chunk.setBlockState(pos, bedrock, false);

                for (int y = -63; y < surface; y++) {
                    pos.set(lx, y, lz);
                    if (anomaly == ANOMALY_CORRUPT_STEP && y == surface - 1 && (lx == 0 || lz == 0) && ((wx + wz) % 3 == 0)) {
                        chunk.setBlockState(pos, glitch, false);
                    } else {
                        chunk.setBlockState(pos, sub, false);
                    }
                }
                pos.set(lx, surface, lz);
                chunk.setBlockState(pos, top, false);
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
        int cx = chunk.getPos().x;
        int cz = chunk.getPos().z;
        int anomaly = getChunkAnomaly(cx, cz);
        RandomSource rng = RandomSource.create(level.getSeed() ^ (cx * 341873128712L + cz * 132897987541L));

        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // 1. Floating midair water pockets (suspended untouched water cubes in thin air!)
        if (!(cx == 0 && cz == 0) && rng.nextFloat() < 0.36F) {
            int px = 3 + rng.nextInt(8);
            int pz = 3 + rng.nextInt(8);
            int py = 86 + rng.nextInt(32);
            int w = 2 + rng.nextInt(3);
            int d = 2 + rng.nextInt(3);
            int h = 1 + rng.nextInt(2);
            BlockState water = Blocks.WATER.defaultBlockState();
            for (int dx = 0; dx < w; dx++) {
                for (int dy = 0; dy < h; dy++) {
                    for (int dz = 0; dz < d; dz++) {
                        pos.set(px + dx, py + dy, pz + dz);
                        if (chunk.getBlockState(pos).isAir()) {
                            chunk.setBlockState(pos, water, false);
                        }
                    }
                }
            }
        }

        if (anomaly == ANOMALY_VOID_HOLE) {
            return;
        }

        // 2. Scattered Glitch Block clusters flickering with every texture in the game
        if (!(cx == 0 && cz == 0) && rng.nextFloat() < 0.28F) {
            int gx = 3 + rng.nextInt(10);
            int gz = 3 + rng.nextInt(10);
            int gy = getSurfaceYAt(minX + gx, minZ + gz) + 1;
            if (gy > 50 && gy < 250) {
                int count = 2 + rng.nextInt(5);
                for (int i = 0; i < count; i++) {
                    int ox = Mth.clamp(gx + rng.nextInt(3) - 1, 0, 15);
                    int oz = Mth.clamp(gz + rng.nextInt(3) - 1, 0, 15);
                    int oy = gy + rng.nextInt(3);
                    chunk.setBlockState(pos.set(ox, oy, oz), ModBlocks.GLITCH_BLOCK.get().defaultBlockState(), false);
                }
            }
        }

        // 3. Overly elongated / stretched glitch trees
        if (!(cx == 0 && cz == 0) && rng.nextFloat() < 0.42F) {
            int tx = 4 + rng.nextInt(8);
            int tz = 4 + rng.nextInt(8);
            int groundY = getSurfaceYAt(minX + tx, minZ + tz);
            if (groundY > 50 && groundY < 220) {
                placeStretchedErrorTree(chunk, tx, groundY + 1, tz, rng);
            }
        }
    }

    private static void placeStretchedErrorTree(ChunkAccess chunk, int lx, int baseY, int lz, RandomSource rng) {
        int height = 16 + rng.nextInt(20);
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState leaves = rng.nextFloat() < 0.25F
                ? ModBlocks.ERROR_GROUND.get().defaultBlockState()
                : Blocks.OAK_LEAVES.defaultBlockState();

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dy = 0; dy < height; dy++) {
            // Occasional missing trunk segment like a chunk/render bug!
            if (dy > 4 && dy < height - 4 && rng.nextFloat() < 0.08F) {
                continue;
            }
            chunk.setBlockState(pos.set(lx, baseY + dy, lz), log, false);
        }

        int crownBase = baseY + height - 6;
        for (int dy = 0; dy <= 7; dy++) {
            int rad = dy < 3 ? 2 : (dy < 6 ? 1 : 0);
            for (int dx = -rad; dx <= rad; dx++) {
                for (int dz = -rad; dz <= rad; dz++) {
                    int px = lx + dx;
                    int pz = lz + dz;
                    if (px < 0 || px > 15 || pz < 0 || pz > 15) continue;
                    if (Math.abs(dx) == 2 && Math.abs(dz) == 2 && rng.nextBoolean()) continue;
                    if (dx == 0 && dz == 0 && dy < 5) continue;
                    chunk.setBlockState(pos.set(px, crownBase + dy, pz), leaves, false);
                }
            }
        }
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        int s = getSurfaceYAt(x, z);
        return s <= -64 ? level.getMinBuildHeight() : s + 1;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
        int surface = getSurfaceYAt(x, z);
        int height = level.getHeight();
        int minY = level.getMinBuildHeight();
        BlockState[] column = new BlockState[height];
        for (int i = 0; i < height; i++) {
            int y = minY + i;
            if (surface <= -64) {
                column[i] = Blocks.AIR.defaultBlockState();
            } else if (y == minY) {
                column[i] = Blocks.BEDROCK.defaultBlockState();
            } else if (y < surface) {
                column[i] = ModBlocks.ERROR_SOIL.get().defaultBlockState();
            } else if (y == surface) {
                column[i] = ModBlocks.ERROR_GROUND.get().defaultBlockState();
            } else {
                column[i] = Blocks.AIR.defaultBlockState();
            }
        }
        return new NoiseColumn(minY, column);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        info.add("The Error Anomaly: " + getChunkAnomaly(pos.getX() >> 4, pos.getZ() >> 4));
    }
}
