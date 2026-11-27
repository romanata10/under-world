package com.underworld.mod.world.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.underworld.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
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

public class TimeChunkGenerator extends ChunkGenerator {
    public static final Codec<TimeChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
            ).apply(instance, TimeChunkGenerator::new)
    );

    public static final int GRID_SIZE = 16;
    public static final int BASE_DISC_Y = 64;

    public TimeChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    private static long cellHash(int cellX, int cellZ) {
        long h = (cellX * 73856093L) ^ (cellZ * 19349663L) ^ 0x54494D45L;
        h ^= (h >>> 17);
        h *= 0xED5AD4BBEL;
        return h;
    }

    public static int getDiscCenterX(int cellX, int cellZ) {
        if (cellX == 0 && cellZ == 0) {
            return 8;
        }
        return cellX * GRID_SIZE + 8;
    }

    public static int getDiscCenterZ(int cellX, int cellZ) {
        if (cellX == 0 && cellZ == 0) {
            return 8;
        }
        return cellZ * GRID_SIZE + 8;
    }

    public static int getDiscY(int cellX, int cellZ) {
        if (cellX == 0 && cellZ == 0) {
            return BASE_DISC_Y;
        }
        long h = cellHash(cellX, cellZ);
        int offset = Math.floorMod((int) (h >>> 5), 7) - 3;
        return BASE_DISC_Y + offset;
    }

    public static int getDiscRadius(int cellX, int cellZ) {
        if (cellX == 0 && cellZ == 0) {
            return 7;
        }
        if (isPortalCell(cellX, cellZ)) {
            return 7;
        }
        long h = cellHash(cellX, cellZ);
        return 6 + Math.floorMod((int) (h >>> 12), 2);
    }

    public static boolean isPortalCell(int cellX, int cellZ) {
        int manhattan = Math.abs(cellX) + Math.abs(cellZ);
        if (manhattan < 6) {
            return false;
        }
        return Math.floorMod(cellX * 13 + cellZ * 29, 17) == 0;
    }

    public static BlockPos findNearestSafeDiscPos(int wx, int wz) {
        int cellX = Math.floorDiv(wx, GRID_SIZE);
        int cellZ = Math.floorDiv(wz, GRID_SIZE);
        return new BlockPos(getDiscCenterX(cellX, cellZ), getDiscY(cellX, cellZ) + 1, getDiscCenterZ(cellX, cellZ));
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
        return 0;
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
        // Because GRID_SIZE == 16 == 1 chunk, each chunk holds its own centered Clock Disc cleanly!
        int cellX = cx;
        int cellZ = cz;
        int centerX = 8;
        int centerZ = 8;
        int discY = getDiscY(cellX, cellZ);
        int radius = getDiscRadius(cellX, cellZ);

        long h = cellHash(cellX, cellZ);
        double hourAng = Math.floorMod((int) h, 12) * (Math.PI / 6.0D);
        double minAng = Math.floorMod((int) (h >>> 8), 12) * (Math.PI / 6.0D);

        BlockState face = ModBlocks.CLOCK_FACE_BLOCK.get().defaultBlockState();
        BlockState rim = ModBlocks.CLOCK_RIM_BLOCK.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // Main floating Clock Disc
        placeClockDiscInChunk(chunk, pos, centerX, discY, centerZ, radius, hourAng, minAng, face, rim);

        // Occasional upper & lower floating clock discs to mirror endless stacked clock rows in image-9.png
        if (Math.floorMod(cellX + cellZ * 2, 2) == 0 && !(cellX == 0 && cellZ == 0)) {
            int lowY = 38 + Math.floorMod((int) (h >>> 16), 9);
            placeClockDiscInChunk(chunk, pos, centerX, lowY, centerZ, 5, minAng, hourAng, face, rim);
        }
        if (Math.floorMod(cellX * 3 - cellZ, 3) == 0 && !(cellX == 0 && cellZ == 0)) {
            int highY = 92 + Math.floorMod((int) (h >>> 20), 12);
            placeClockDiscInChunk(chunk, pos, centerX, highY, centerZ, 5, hourAng + 1.1D, minAng + 0.5D, face, rim);
        }

        // Rare Portal Clock Disc containing escape portal arch back to the Overworld!
        if (isPortalCell(cellX, cellZ)) {
            placeEscapePortalArch(chunk, pos, centerX, discY, centerZ);
        }

        return CompletableFuture.completedFuture(chunk);
    }

    private static void placeClockDiscInChunk(ChunkAccess chunk, BlockPos.MutableBlockPos pos,
                                              int cx, int y, int cz, int radius,
                                              double hourAng, double minAng,
                                              BlockState face, BlockState rim) {
        double rSq = (radius + 0.35D) * (radius + 0.35D);
        double innerSq = (radius - 1.1D) * (radius - 1.1D);
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                double dx = lx - cx;
                double dz = lz - cz;
                double dSq = dx * dx + dz * dz;
                if (dSq > rSq) {
                    continue;
                }
                boolean isBorder = dSq >= innerSq;
                chunk.setBlockState(pos.set(lx, y - 1, lz), rim, false);
                chunk.setBlockState(pos.set(lx, y, lz), isBorder ? rim : face, false);
            }
        }
        // Center clock axle + hands on dial face
        chunk.setBlockState(pos.set(cx, y, cz), rim, false);
        drawHand(chunk, pos, cx, y, cz, hourAng, Math.max(2, radius - 3), rim);
        drawHand(chunk, pos, cx, y, cz, minAng, Math.max(3, radius - 1), rim);
    }

    private static void drawHand(ChunkAccess chunk, BlockPos.MutableBlockPos pos,
                                 int cx, int y, int cz, double angle, int length, BlockState state) {
        double dirX = Math.cos(angle);
        double dirZ = Math.sin(angle);
        for (int step = 1; step <= length; step++) {
            int x = Mth.clamp((int) Math.round(cx + dirX * step), 1, 14);
            int z = Mth.clamp((int) Math.round(cz + dirZ * step), 1, 14);
            chunk.setBlockState(pos.set(x, y, z), state, false);
        }
    }

    private static void placeEscapePortalArch(ChunkAccess chunk, BlockPos.MutableBlockPos pos,
                                              int cx, int baseY, int cz) {
        BlockState rim = ModBlocks.CLOCK_RIM_BLOCK.get().defaultBlockState();
        BlockState portal = ModBlocks.TIME_PORTAL.get().defaultBlockState();
        BlockState lantern = Blocks.SOUL_LANTERN.defaultBlockState();

        // Grand 5-wide x 6-tall dark bronze clock portal frame standing on the disc
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = 1; dy <= 5; dy++) {
                boolean border = Math.abs(dx) == 2 || dy == 5;
                boolean cornersTop = Math.abs(dx) == 2 && dy == 5;
                if (cornersTop) {
                    continue;
                }
                chunk.setBlockState(pos.set(cx + dx, baseY + dy, cz), border ? rim : portal, false);
            }
        }
        chunk.setBlockState(pos.set(cx - 2, baseY + 1, cz - 1), lantern, false);
        chunk.setBlockState(pos.set(cx + 2, baseY + 1, cz - 1), lantern, false);
        chunk.setBlockState(pos.set(cx - 2, baseY + 1, cz + 1), lantern, false);
        chunk.setBlockState(pos.set(cx + 2, baseY + 1, cz + 1), lantern, false);
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        int cellX = Math.floorDiv(x, GRID_SIZE);
        int cellZ = Math.floorDiv(z, GRID_SIZE);
        int lx = Math.floorMod(x, GRID_SIZE) - 8;
        int lz = Math.floorMod(z, GRID_SIZE) - 8;
        int r = getDiscRadius(cellX, cellZ);
        if (lx * lx + lz * lz <= r * r) {
            return getDiscY(cellX, cellZ) + 1;
        }
        return level.getMinBuildHeight();
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
        int height = level.getHeight();
        int minY = level.getMinBuildHeight();
        BlockState[] col = new BlockState[height];
        for (int i = 0; i < height; i++) {
            col[i] = Blocks.AIR.defaultBlockState();
        }
        int cellX = Math.floorDiv(x, GRID_SIZE);
        int cellZ = Math.floorDiv(z, GRID_SIZE);
        int lx = Math.floorMod(x, GRID_SIZE) - 8;
        int lz = Math.floorMod(z, GRID_SIZE) - 8;
        int r = getDiscRadius(cellX, cellZ);
        if (lx * lx + lz * lz <= r * r) {
            int dy = getDiscY(cellX, cellZ);
            if (dy - minY >= 1 && dy - minY < height) {
                col[dy - minY] = ModBlocks.CLOCK_FACE_BLOCK.get().defaultBlockState();
                col[dy - minY - 1] = ModBlocks.CLOCK_RIM_BLOCK.get().defaultBlockState();
            }
        }
        return new NoiseColumn(minY, col);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        int cx = Math.floorDiv(pos.getX(), GRID_SIZE);
        int cz = Math.floorDiv(pos.getZ(), GRID_SIZE);
        info.add("The Time Disc: (" + cx + "," + cz + ") portal=" + isPortalCell(cx, cz));
    }
}
