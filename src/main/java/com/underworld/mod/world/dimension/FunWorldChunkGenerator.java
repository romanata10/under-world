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

public class FunWorldChunkGenerator extends ChunkGenerator {
    public static final Codec<FunWorldChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
            ).apply(instance, FunWorldChunkGenerator::new)
    );

    public FunWorldChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    public static int getSurfaceY(int wx, int wz) {
        double h1 = Math.sin(wx * 0.028D) * Math.cos(wz * 0.026D) * 6.5D;
        double h2 = Math.sin((wx + wz) * 0.015D) * 3.5D;
        double h3 = Math.cos(wx * 0.07D - wz * 0.05D) * 1.6D;
        return Mth.clamp((int) Math.round(72.0D + h1 + h2 + h3), 64, 88);
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
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockState dirt = ModBlocks.FUN_DIRT.get().defaultBlockState();
        BlockState grass = ModBlocks.FUN_GRASS_BLOCK.get().defaultBlockState();

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int wx = minX + lx;
                int wz = minZ + lz;
                int surfaceY = getSurfaceY(wx, wz);

                pos.set(lx, -64, lz);
                chunk.setBlockState(pos, bedrock, false);

                for (int y = -63; y < surfaceY - 3; y++) {
                    pos.set(lx, y, lz);
                    chunk.setBlockState(pos, stone, false);
                }
                for (int y = surfaceY - 3; y < surfaceY; y++) {
                    pos.set(lx, y, lz);
                    chunk.setBlockState(pos, dirt, false);
                }
                pos.set(lx, surfaceY, lz);
                chunk.setBlockState(pos, grass, false);
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
        RandomSource rng = RandomSource.create(level.getSeed() ^ (cx * 341873128712L + cz * 132897987541L));

        boolean hasMazeGateway = (cx == 5 && cz == 0)
                || (Math.floorMod(cx, 12) == 5 && Math.floorMod(cz, 12) == 0);

        if (hasMazeGateway) {
            placeMazeGatewayInChunk(chunk, minX, minZ);
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 30; i++) {
            int lx = rng.nextInt(16);
            int lz = rng.nextInt(16);
            if (hasMazeGateway && lx >= 2 && lx <= 12 && lz >= 2 && lz <= 12) {
                continue;
            }
            int wx = minX + lx;
            int wz = minZ + lz;
            int surfaceY = getSurfaceY(wx, wz);

            pos.set(lx, surfaceY, lz);
            if (!chunk.getBlockState(pos).is(ModBlocks.FUN_GRASS_BLOCK.get())) {
                continue;
            }
            pos.set(lx, surfaceY + 1, lz);
            if (!chunk.getBlockState(pos).isAir()) {
                continue;
            }

            int roll = rng.nextInt(100);
            BlockState plant;
            if (roll < 34) {
                plant = ModBlocks.FUN_GRASS_TUFT.get().defaultBlockState();
            } else if (roll < 50) {
                plant = ModBlocks.FUN_YELLOW_FLOWER.get().defaultBlockState();
            } else if (roll < 64) {
                plant = ModBlocks.FUN_BLUE_FLOWER.get().defaultBlockState();
            } else if (roll < 74) {
                plant = ModBlocks.FUN_FLAT_RED_FLOWER.get().defaultBlockState();
            } else if (roll < 84) {
                plant = ModBlocks.FUN_FLAT_BLUE_FLOWER.get().defaultBlockState();
            } else if (roll < 93) {
                plant = ModBlocks.FUN_FLAT_WHITE_FLOWER.get().defaultBlockState();
            } else if (roll < 97) {
                plant = ModBlocks.FUN_MUSHROOM_BLUE.get().defaultBlockState();
            } else {
                plant = ModBlocks.FUN_MUSHROOM_RED.get().defaultBlockState();
            }
            chunk.setBlockState(pos, plant, false);
        }
    }

    private void placeMazeGatewayInChunk(ChunkAccess chunk, int minX, int minZ) {
        int baseY = getSurfaceY(minX + 7, minZ + 7);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        BlockState brick = ModBlocks.MAZE_BRICK.get().defaultBlockState();
        BlockState floor = ModBlocks.MAZE_FLOOR.get().defaultBlockState();
        BlockState ceiling = ModBlocks.MAZE_CEILING.get().defaultBlockState();
        BlockState portal = ModBlocks.MAZE_PORTAL.get().defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState dirt = ModBlocks.FUN_DIRT.get().defaultBlockState();

        // Build a 9x6x9 ruin strictly inside local [3..11] x [3..11]
        for (int lx = 3; lx <= 11; lx++) {
            for (int lz = 3; lz <= 11; lz++) {
                // Fill foundation down into hills
                for (int y = baseY - 6; y <= baseY; y++) {
                    pos.set(lx, y, lz);
                    chunk.setBlockState(pos, y == baseY ? floor : dirt, false);
                }
                for (int y = baseY + 1; y <= baseY + 6; y++) {
                    pos.set(lx, y, lz);
                    boolean isPerimeter = (lx == 3 || lx == 11 || lz == 3 || lz == 11);
                    boolean isDoorway = (lz == 3 && lx >= 6 && lx <= 8 && y <= baseY + 3);
                    if (y == baseY + 5) {
                        chunk.setBlockState(pos, ceiling, false);
                    } else if (y == baseY + 6) {
                        chunk.setBlockState(pos, isPerimeter ? brick : air, false);
                    } else if (isPerimeter && !isDoorway) {
                        chunk.setBlockState(pos, brick, false);
                    } else if (lz == 9 && lx >= 6 && lx <= 8 && y >= baseY + 1 && y <= baseY + 3) {
                        chunk.setBlockState(pos, portal, false);
                    } else {
                        chunk.setBlockState(pos, air, false);
                    }
                }
            }
        }
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        return getSurfaceY(x, z) + 1;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
        int surface = getSurfaceY(x, z);
        int height = level.getHeight();
        int minY = level.getMinBuildHeight();
        BlockState[] column = new BlockState[height];
        for (int i = 0; i < height; i++) {
            int y = minY + i;
            if (y == minY) {
                column[i] = Blocks.BEDROCK.defaultBlockState();
            } else if (y < surface - 3) {
                column[i] = Blocks.STONE.defaultBlockState();
            } else if (y < surface) {
                column[i] = ModBlocks.FUN_DIRT.get().defaultBlockState();
            } else if (y == surface) {
                column[i] = ModBlocks.FUN_GRASS_BLOCK.get().defaultBlockState();
            } else {
                column[i] = Blocks.AIR.defaultBlockState();
            }
        }
        return new NoiseColumn(minY, column);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        info.add("FunWorld Height: " + getSurfaceY(pos.getX(), pos.getZ()));
    }
}
