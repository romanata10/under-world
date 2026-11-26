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

public class MazeChunkGenerator extends ChunkGenerator {
    public static final Codec<MazeChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
            ).apply(instance, MazeChunkGenerator::new)
    );

    public static final int FLOOR_Y = 64;
    public static final int CEILING_Y = 69;

    public MazeChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    private static long cellHash(int cx, int cz) {
        long h = cx * 73856093L ^ cz * 19349663L ^ 0x5DEECE66DL;
        h ^= (h >>> 17);
        h *= 0xED5AD4BB66D13D5DL;
        h ^= (h >>> 31);
        return h;
    }

    private static boolean isOpenEast(int cx, int cz) {
        if ((cx == 0 || cx == 1 || cx == 2) && (cz == 0 || cz == 1)) {
            return true;
        }
        long h = cellHash(cx, cz);
        boolean primaryEast = (h & 1L) == 0L;
        boolean loop = ((h >>> 4) & 3L) == 0L;
        return primaryEast || loop;
    }

    private static boolean isOpenSouth(int cx, int cz) {
        if ((cx == 0 || cx == 1) && (cz == 0 || cz == 1)) {
            return true;
        }
        long h = cellHash(cx, cz);
        boolean primarySouth = (h & 1L) == 1L;
        boolean loop = ((h >>> 7) & 3L) == 0L;
        return primarySouth || loop;
    }

    private static boolean isExitCell(int cx, int cz) {
        if (cx == 6 && cz == 1) {
            return true;
        }
        return Math.floorMod(cx, 7) == 5 && Math.floorMod(cz, 7) == 5 && !(cx == 1 && cz == 1);
    }

    public static boolean isCorridorAir(int wx, int wz) {
        int cellX = Math.floorDiv(wx, 8);
        int cellZ = Math.floorDiv(wz, 8);
        int lx = Math.floorMod(wx, 8);
        int lz = Math.floorMod(wz, 8);

        // Keep center 6x6 open in every cell
        if (lx < 6 && lz < 6) {
            return true;
        }
        // Keep 2x2 brick corner pillars solid
        if (lx >= 6 && lz >= 6) {
            return false;
        }
        if (lx >= 6) {
            return isOpenEast(cellX, cellZ);
        }
        return isOpenSouth(cellX, cellZ);
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
        return 128;
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState random,
                                                        StructureManager structureManager, ChunkAccess chunk) {
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        BlockState brick = ModBlocks.MAZE_BRICK.get().defaultBlockState();
        BlockState floor = ModBlocks.MAZE_FLOOR.get().defaultBlockState();
        BlockState ceiling = ModBlocks.MAZE_CEILING.get().defaultBlockState();
        BlockState officeLight = Blocks.PEARLESCENT_FROGLIGHT.defaultBlockState();
        BlockState teleporter = ModBlocks.MAZE_TELEPORTER.get().defaultBlockState();
        BlockState beaconLight = Blocks.SEA_LANTERN.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int wx = minX + lx;
                int wz = minZ + lz;
                int cellX = Math.floorDiv(wx, 8);
                int cellZ = Math.floorDiv(wz, 8);
                int cellLx = Math.floorMod(wx, 8);
                int cellLz = Math.floorMod(wz, 8);

                boolean corridor = isCorridorAir(wx, wz);
                boolean exitCenter = isExitCell(cellX, cellZ) && cellLx == 3 && cellLz == 3;
                boolean dimTileLight = (cellLx == 3 && cellLz == 3)
                        && (isExitCell(cellX, cellZ) || (Math.floorMod(cellX * 3 + cellZ * 5, 4) == 0));

                pos.set(lx, 0, lz);
                chunk.setBlockState(pos, bedrock, false);

                pos.set(lx, FLOOR_Y - 1, lz);
                chunk.setBlockState(pos, bedrock, false);

                pos.set(lx, FLOOR_Y, lz);
                chunk.setBlockState(pos, exitCenter ? teleporter : floor, false);

                for (int y = FLOOR_Y + 1; y < CEILING_Y; y++) {
                    pos.set(lx, y, lz);
                    if (exitCenter && y == FLOOR_Y + 1) {
                        chunk.setBlockState(pos, air, false);
                    } else {
                        chunk.setBlockState(pos, corridor ? air : brick, false);
                    }
                }

                pos.set(lx, CEILING_Y, lz);
                if (exitCenter) {
                    chunk.setBlockState(pos, beaconLight, false);
                } else {
                    chunk.setBlockState(pos, dimTileLight ? officeLight : ceiling, false);
                }

                pos.set(lx, CEILING_Y + 1, lz);
                chunk.setBlockState(pos, bedrock, false);
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        return FLOOR_Y + 1;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
        int height = level.getHeight();
        int minY = level.getMinBuildHeight();
        BlockState[] column = new BlockState[height];
        boolean corridor = isCorridorAir(x, z);
        for (int i = 0; i < height; i++) {
            int y = minY + i;
            if (y == 0 || y == FLOOR_Y - 1 || y == CEILING_Y + 1) {
                column[i] = Blocks.BEDROCK.defaultBlockState();
            } else if (y == FLOOR_Y) {
                column[i] = ModBlocks.MAZE_FLOOR.get().defaultBlockState();
            } else if (y == CEILING_Y) {
                column[i] = ModBlocks.MAZE_CEILING.get().defaultBlockState();
            } else if (y > FLOOR_Y && y < CEILING_Y) {
                column[i] = corridor ? Blocks.AIR.defaultBlockState() : ModBlocks.MAZE_BRICK.get().defaultBlockState();
            } else {
                column[i] = Blocks.AIR.defaultBlockState();
            }
        }
        return new NoiseColumn(minY, column);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        info.add("Maze Cell: (" + Math.floorDiv(pos.getX(), 8) + ", " + Math.floorDiv(pos.getZ(), 8) + ")");
    }
}
