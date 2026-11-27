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

public class XpDesktopChunkGenerator extends ChunkGenerator {
    public static final int DESKTOP_Y = 64;

    public static final Codec<XpDesktopChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
            ).apply(instance, XpDesktopChunkGenerator::new)
    );

    public XpDesktopChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    public static int getSurfaceY(int wx, int wz) {
        if (wz >= 12) {
            // Lush green Windows XP Bliss hills behind player!
            double rise = Math.min(20.0D, (wz - 12) * 0.42D);
            double wave = Math.sin(wx * 0.035D) * 4.5D + Math.cos(wz * 0.045D) * 2.5D;
            return Mth.clamp((int) Math.round(DESKTOP_Y + rise + Math.max(0.0D, wave)), DESKTOP_Y, 92);
        }
        return DESKTOP_Y;
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
        BlockState dirt = Blocks.DIRT.defaultBlockState();
        BlockState xpGrass = ModBlocks.XP_GRASS_BLOCK.get().defaultBlockState();
        BlockState desktopBlue = Blocks.LIGHT_BLUE_CONCRETE.defaultBlockState();
        BlockState taskbarBlue = Blocks.BLUE_CONCRETE.defaultBlockState();
        BlockState startGreen = Blocks.LIME_CONCRETE.defaultBlockState();

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int wx = minX + lx;
                int wz = minZ + lz;
                int surfaceY = getSurfaceY(wx, wz);

                pos.set(lx, -64, lz);
                chunk.setBlockState(pos, bedrock, false);
                for (int y = -63; y < surfaceY - 2; y++) {
                    pos.set(lx, y, lz);
                    chunk.setBlockState(pos, Blocks.STONE.defaultBlockState(), false);
                }
                for (int y = surfaceY - 2; y < surfaceY; y++) {
                    pos.set(lx, y, lz);
                    chunk.setBlockState(pos, dirt, false);
                }
                pos.set(lx, surfaceY, lz);
                if (wz >= 12) {
                    chunk.setBlockState(pos, xpGrass, false);
                } else if (wz <= -24 && wz >= -27) {
                    chunk.setBlockState(pos, (wx >= -28 && wx <= -18) ? startGreen : taskbarBlue, false);
                } else {
                    chunk.setBlockState(pos, desktopBlue, false);
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

        // Chrome colorful portal orb at center-ahead (0, 65, -14)
        if (cx == 0 && cz == -1) {
            placeChromeMonument(level, new BlockPos(0, DESKTOP_Y, -14));
        }

        // Return CRT Computer near spawn (8, 65, 10)
        if (cx == 0 && cz == 0) {
            level.setBlock(new BlockPos(10, DESKTOP_Y + 1, 9), ModBlocks.COMPUTER.get().defaultBlockState(), 2);
        }

        // Floating 3D Desktop Shortcut Icons scattered across the desktop plane (wz < 10)
        if (cz >= -2 && cz <= 0 && Math.abs(cx) <= 2 && !(cx == 0 && cz == -1)) {
            int ix = minX + 7;
            int iz = minZ + 7;
            if (iz < 8 && !(Math.abs(ix) <= 5 && Math.abs(iz + 14) <= 5)) {
                placeDesktopIcon(level, new BlockPos(ix, DESKTOP_Y, iz));
            }
        }
    }

    private static void placeDesktopIcon(WorldGenLevel level, BlockPos base) {
        BlockState icon = ModBlocks.XP_ICON_BLOCK.get().defaultBlockState();
        // Elevated icon cube (y + 3 .. y + 5) so approaching drops it!
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 3; dy <= 5; dy++) {
                level.setBlock(base.offset(dx, dy, 0), icon, 2);
            }
        }
        level.setBlock(base.offset(-1, 1, 0), Blocks.WHITE_CONCRETE.defaultBlockState(), 2);
        level.setBlock(base.offset(0, 1, 0), Blocks.WHITE_CONCRETE.defaultBlockState(), 2);
        level.setBlock(base.offset(1, 1, 0), Blocks.WHITE_CONCRETE.defaultBlockState(), 2);
    }

    private static void placeChromeMonument(WorldGenLevel level, BlockPos base) {
        // Giant colorful Chrome logo frame & walk-in portal core!
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = 1; dy <= 5; dy++) {
                boolean inner = Math.abs(dx) <= 1 && dy >= 2 && dy <= 4;
                if (inner) {
                    level.setBlock(base.offset(dx, dy, 0), ModBlocks.CHROME_PORTAL.get().defaultBlockState(), 2);
                } else {
                    BlockState col;
                    if (dy >= 4) col = Blocks.RED_CONCRETE.defaultBlockState();
                    else if (dx >= 1) col = Blocks.YELLOW_CONCRETE.defaultBlockState();
                    else col = Blocks.LIME_CONCRETE.defaultBlockState();
                    level.setBlock(base.offset(dx, dy, 0), col, 2);
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
        BlockState[] states = new BlockState[height];
        int minY = level.getMinBuildHeight();
        for (int i = 0; i < height; i++) {
            int y = minY + i;
            if (y == -64) states[i] = Blocks.BEDROCK.defaultBlockState();
            else if (y < surface) states[i] = Blocks.DIRT.defaultBlockState();
            else if (y == surface) states[i] = z >= 12 ? ModBlocks.XP_GRASS_BLOCK.get().defaultBlockState() : Blocks.LIGHT_BLUE_CONCRETE.defaultBlockState();
            else states[i] = Blocks.AIR.defaultBlockState();
        }
        return new NoiseColumn(minY, states);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        info.add("Under World: The Xp");
    }
}
