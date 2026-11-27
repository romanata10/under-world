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

public class PlainsChunkGenerator extends ChunkGenerator {
    public static final Codec<PlainsChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
            ).apply(instance, PlainsChunkGenerator::new)
    );

    public PlainsChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    public static int getPathZ(int wx) {
        return (int) Math.round(Math.sin(wx * 0.020D) * 14.0D + Math.cos(wx * 0.008D) * 8.0D);
    }

    public static int getSurfaceY(int wx, int wz) {
        double rolling = Math.sin(wx * 0.014D) * Math.cos(wz * 0.016D) * 7.0D
                + Math.sin((wx - wz) * 0.008D) * 5.0D
                + Math.cos(wz * 0.032D) * 2.2D;

        int modX = Math.floorMod(wx, 512);
        double dxHill = Math.abs(modX - 420);
        double dzHill = Math.abs(wz);
        double hillDist = Math.hypot(dxHill, dzHill);
        double turbineHill = hillDist < 52.0D ? (1.0D - hillDist / 52.0D) * 14.0D : 0.0D;

        int pathZ = getPathZ(wx);
        if (Math.abs(wz - pathZ) <= 3) {
            rolling *= 0.45D;
        }
        return Mth.clamp((int) Math.round(68.0D + rolling + turbineHill), 64, 96);
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
        BlockState dirt = Blocks.DIRT.defaultBlockState();
        BlockState grass = ModBlocks.PLAINS_GRASS_BLOCK.get().defaultBlockState();
        BlockState path = ModBlocks.PLAINS_PATH_BLOCK.get().defaultBlockState();

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int wx = minX + lx;
                int wz = minZ + lz;
                int surfaceY = getSurfaceY(wx, wz);
                int pathZ = getPathZ(wx);
                boolean isPath = Math.abs(wz - pathZ) <= 1;

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
                chunk.setBlockState(pos, isPath ? path : grass, false);
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
        RandomSource rng = RandomSource.create(level.getSeed() ^ (cx * 4812713L + cz * 9123711L));

        // Return beacon pad strictly inside chunk (0, cz containing rz)
        int rz = getPathZ(8) + 4;
        int ry = getSurfaceY(8, rz);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                setInChunk(chunk, minX, minZ, 8 + dx, ry, rz + dz, ModBlocks.PLAINS_PORTAL_GRASS.get().defaultBlockState());
            }
        }

        // Sparse wild tall grass along rolling meadow hills
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 16; i++) {
            int lx = rng.nextInt(16);
            int lz = rng.nextInt(16);
            int wx = minX + lx;
            int wz = minZ + lz;
            if (Math.abs(wz - getPathZ(wx)) <= 3) continue;
            int y = getSurfaceY(wx, wz);
            if (y + 1 < 320 && chunk.getBlockState(mpos.set(lx, y + 1, lz)).isAir()) {
                chunk.setBlockState(mpos, Blocks.GRASS.defaultBlockState(), false);
            }
        }

        // Static Power Pylons every 3 chunks strictly inside current chunk!
        if (Math.floorMod(cx, 3) == 1) {
            int px = minX + 8;
            int side = (Math.floorMod(cx / 3, 2) == 0) ? -10 : 10;
            int pz = getPathZ(px) + side;
            if (pz >= minZ + 3 && pz <= minZ + 12) {
                placeStaticPylonInChunk(chunk, minX, minZ, new BlockPos(px, getSurfaceY(px, pz), pz));
            }
        }

        // Abandoned houses & apartment blocks periodically along the road
        if (Math.floorMod(cx, 5) == 3 && Math.floorMod(cx, 32) != 26) {
            int bx = minX + 8;
            int side = (Math.floorMod(cx / 5, 2) == 0) ? 14 : -14;
            int bz = getPathZ(bx) + side;
            if (bz >= minZ + 5 && bz <= minZ + 10) {
                int by = getSurfaceY(bx, bz);
                if (Math.floorMod(cx, 10) == 8) {
                    placeApartmentBlockInChunk(chunk, minX, minZ, new BlockPos(bx, by, bz), rng);
                } else {
                    placeAbandonedHouseInChunk(chunk, minX, minZ, new BlockPos(bx, by, bz), rng);
                }
            }
        }

        // Distant Wind Turbines atop hills (every 512 blocks along X near z=0) -> clip-safe across chunk edges!
        int sectorX = Math.floorDiv(minX + 8, 512) * 512;
        int portalTx = sectorX + 420;
        placeDistantWindTurbineInChunk(chunk, minX, minZ, new BlockPos(portalTx, getSurfaceY(portalTx, 0), 0), true);
        placeDistantWindTurbineInChunk(chunk, minX, minZ, new BlockPos(portalTx + 24, getSurfaceY(portalTx + 24, -18), -18), false);
        placeDistantWindTurbineInChunk(chunk, minX, minZ, new BlockPos(portalTx + 26, getSurfaceY(portalTx + 26, 20), 20), false);
    }

    private static void placeStaticPylonInChunk(ChunkAccess chunk, int minX, int minZ, BlockPos base) {
        BlockState steel = ModBlocks.PYLON_STEEL.get().defaultBlockState();
        BlockState bars = Blocks.IRON_BARS.defaultBlockState();
        BlockState chain = Blocks.CHAIN.defaultBlockState();

        for (int dy = 0; dy <= 7; dy++) {
            int r = dy < 3 ? 2 : 1;
            setInChunk(chunk, minX, minZ, base.getX() - r, base.getY() + dy, base.getZ() - r, steel);
            setInChunk(chunk, minX, minZ, base.getX() + r, base.getY() + dy, base.getZ() - r, steel);
            setInChunk(chunk, minX, minZ, base.getX() - r, base.getY() + dy, base.getZ() + r, steel);
            setInChunk(chunk, minX, minZ, base.getX() + r, base.getY() + dy, base.getZ() + r, steel);
        }
        for (int dy = 8; dy <= 22; dy++) {
            setInChunk(chunk, minX, minZ, base.getX(), base.getY() + dy, base.getZ(), steel);
            if (dy % 3 == 0) {
                setInChunk(chunk, minX, minZ, base.getX(), base.getY() + dy, base.getZ() - 1, bars);
                setInChunk(chunk, minX, minZ, base.getX(), base.getY() + dy, base.getZ() + 1, bars);
            }
        }
        for (int dz = -5; dz <= 5; dz++) {
            setInChunk(chunk, minX, minZ, base.getX(), base.getY() + 15, base.getZ() + dz, steel);
        }
        for (int dz = -4; dz <= 4; dz++) {
            setInChunk(chunk, minX, minZ, base.getX(), base.getY() + 20, base.getZ() + dz, steel);
        }
        setInChunk(chunk, minX, minZ, base.getX(), base.getY() + 14, base.getZ() - 5, chain);
        setInChunk(chunk, minX, minZ, base.getX(), base.getY() + 13, base.getZ() - 5, chain);
        setInChunk(chunk, minX, minZ, base.getX(), base.getY() + 14, base.getZ() + 5, chain);
        setInChunk(chunk, minX, minZ, base.getX(), base.getY() + 13, base.getZ() + 5, chain);
        setInChunk(chunk, minX, minZ, base.getX(), base.getY() + 23, base.getZ(), Blocks.REDSTONE_TORCH.defaultBlockState());
    }

    private static void placeAbandonedHouseInChunk(ChunkAccess chunk, int minX, int minZ, BlockPos base, RandomSource rng) {
        BlockState wall = Blocks.COBBLESTONE.defaultBlockState();
        BlockState pillar = Blocks.OAK_LOG.defaultBlockState();
        BlockState floor = Blocks.SPRUCE_PLANKS.defaultBlockState();
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                setInChunk(chunk, minX, minZ, base.getX() + dx, base.getY(), base.getZ() + dz, floor);
                setInChunk(chunk, minX, minZ, base.getX() + dx, base.getY() - 1, base.getZ() + dz, Blocks.DIRT.defaultBlockState());
                for (int dy = 1; dy <= 5; dy++) {
                    boolean isEdge = Math.abs(dx) == 4 || Math.abs(dz) == 4;
                    if (dy == 5) {
                        setInChunk(chunk, minX, minZ, base.getX() + dx, base.getY() + dy, base.getZ() + dz, wall);
                    } else if (isEdge) {
                        boolean isCorner = Math.abs(dx) == 4 && Math.abs(dz) == 4;
                        boolean isDoor = dx == 0 && dz == -4 && (dy == 1 || dy == 2);
                        boolean isWindow = (Math.abs(dx) == 2 || Math.abs(dz) == 2) && dy == 2;
                        if (isDoor) {
                            setInChunk(chunk, minX, minZ, base.getX() + dx, base.getY() + dy, base.getZ() + dz, Blocks.AIR.defaultBlockState());
                        } else if (isWindow) {
                            setInChunk(chunk, minX, minZ, base.getX() + dx, base.getY() + dy, base.getZ() + dz,
                                    rng.nextBoolean() ? Blocks.GLASS_PANE.defaultBlockState() : Blocks.AIR.defaultBlockState());
                        } else {
                            setInChunk(chunk, minX, minZ, base.getX() + dx, base.getY() + dy, base.getZ() + dz, isCorner ? pillar : wall);
                        }
                    } else {
                        setInChunk(chunk, minX, minZ, base.getX() + dx, base.getY() + dy, base.getZ() + dz, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        setInChunk(chunk, minX, minZ, base.getX() + 2, base.getY() + 1, base.getZ() + 2, Blocks.COBWEB.defaultBlockState());
        setInChunk(chunk, minX, minZ, base.getX() - 2, base.getY() + 1, base.getZ() + 2, Blocks.CRAFTING_TABLE.defaultBlockState());
    }

    private static void placeApartmentBlockInChunk(ChunkAccess chunk, int minX, int minZ, BlockPos base, RandomSource rng) {
        BlockState concrete = Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
        BlockState trim = Blocks.GRAY_CONCRETE.defaultBlockState();
        BlockState floor = Blocks.STONE_BRICKS.defaultBlockState();
        int floors = 4;
        for (int fl = 0; fl < floors; fl++) {
            int y0 = fl * 4;
            for (int dx = -5; dx <= 5; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    setInChunk(chunk, minX, minZ, base.getX() + dx, base.getY() + y0, base.getZ() + dz, floor);
                    for (int dy = 1; dy <= 3; dy++) {
                        boolean wall = Math.abs(dx) == 5 || Math.abs(dz) == 4;
                        boolean doorway = fl == 0 && dx == 0 && dz == -4 && dy <= 2;
                        boolean window = wall && dy == 2 && (Math.abs(dx) == 2 || Math.abs(dz) == 2);
                        if (doorway) {
                            setInChunk(chunk, minX, minZ, base.getX() + dx, base.getY() + y0 + dy, base.getZ() + dz, Blocks.AIR.defaultBlockState());
                        } else if (window) {
                            setInChunk(chunk, minX, minZ, base.getX() + dx, base.getY() + y0 + dy, base.getZ() + dz,
                                    rng.nextFloat() < 0.45F ? Blocks.GLASS_PANE.defaultBlockState() : Blocks.AIR.defaultBlockState());
                        } else if (wall) {
                            boolean corner = Math.abs(dx) == 5 && Math.abs(dz) == 4;
                            setInChunk(chunk, minX, minZ, base.getX() + dx, base.getY() + y0 + dy, base.getZ() + dz, corner ? trim : concrete);
                        } else {
                            boolean stairWell = dx >= 2 && dz >= 1;
                            if (!stairWell || dy < 3) {
                                setInChunk(chunk, minX, minZ, base.getX() + dx, base.getY() + y0 + dy, base.getZ() + dz, Blocks.AIR.defaultBlockState());
                            }
                        }
                    }
                }
            }
            setInChunk(chunk, minX, minZ, base.getX() + 3, base.getY() + y0 + 1, base.getZ() + 2, Blocks.STONE_SLAB.defaultBlockState());
            setInChunk(chunk, minX, minZ, base.getX() + 3, base.getY() + y0 + 2, base.getZ() + 1, Blocks.STONE_BRICKS.defaultBlockState());
            setInChunk(chunk, minX, minZ, base.getX() + 2, base.getY() + y0 + 3, base.getZ() + 1, Blocks.STONE_SLAB.defaultBlockState());
        }
        int roofY = floors * 4;
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                setInChunk(chunk, minX, minZ, base.getX() + dx, base.getY() + roofY, base.getZ() + dz, trim);
                if (Math.abs(dx) == 5 || Math.abs(dz) == 4) {
                    setInChunk(chunk, minX, minZ, base.getX() + dx, base.getY() + roofY + 1, base.getZ() + dz, concrete);
                }
            }
        }
        for (int dy = 1; dy <= 5; dy++) {
            setInChunk(chunk, minX, minZ, base.getX(), base.getY() + roofY + dy, base.getZ(), Blocks.IRON_BARS.defaultBlockState());
        }
        setInChunk(chunk, minX, minZ, base.getX(), base.getY() + roofY + 6, base.getZ(), Blocks.REDSTONE_TORCH.defaultBlockState());
    }

    private static void placeDistantWindTurbineInChunk(ChunkAccess chunk, int minX, int minZ, BlockPos base, boolean withPortal) {
        // Quick AABB chunk rejection so zero overhead outside nearby chunks
        if (base.getX() + 4 < minX || base.getX() - 4 >= minX + 16
                || base.getZ() + 12 < minZ || base.getZ() - 12 >= minZ + 16) {
            return;
        }
        BlockState pillar = ModBlocks.TURBINE_PILLAR.get().defaultBlockState();
        BlockState blade = ModBlocks.TURBINE_BLADE.get().defaultBlockState();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                setInChunk(chunk, minX, minZ, base.getX() + dx, base.getY(), base.getZ() + dz, Blocks.SMOOTH_STONE.defaultBlockState());
                setInChunk(chunk, minX, minZ, base.getX() + dx, base.getY() - 1, base.getZ() + dz, Blocks.STONE.defaultBlockState());
            }
        }
        for (int dy = 1; dy <= 22; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (Math.abs(dx) == 1 && Math.abs(dz) == 1) continue;
                    setInChunk(chunk, minX, minZ, base.getX() + dx, base.getY() + dy, base.getZ() + dz, pillar);
                }
            }
        }
        for (int dx = -3; dx <= 2; dx++) {
            for (int dy = 21; dy <= 23; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    setInChunk(chunk, minX, minZ, base.getX() + dx, base.getY() + dy, base.getZ() + dz, pillar);
                }
            }
        }
        for (int i = 1; i <= 11; i++) {
            setInChunk(chunk, minX, minZ, base.getX() - 3, base.getY() + 22 + i, base.getZ(), blade);
            setInChunk(chunk, minX, minZ, base.getX() - 3, base.getY() + 22 - i / 2, base.getZ() + i, blade);
            setInChunk(chunk, minX, minZ, base.getX() - 3, base.getY() + 22 - i / 2, base.getZ() - i, blade);
        }
        setInChunk(chunk, minX, minZ, base.getX() + 2, base.getY() + 24, base.getZ(), Blocks.REDSTONE_TORCH.defaultBlockState());

        if (withPortal) {
            for (int dy = 1; dy <= 3; dy++) {
                setInChunk(chunk, minX, minZ, base.getX() - 1, base.getY() + dy, base.getZ(), Blocks.AIR.defaultBlockState());
                setInChunk(chunk, minX, minZ, base.getX(), base.getY() + dy, base.getZ(), ModBlocks.WIND_TURBINE_PORTAL.get().defaultBlockState());
            }
            setInChunk(chunk, minX, minZ, base.getX() - 2, base.getY() + 1, base.getZ(), Blocks.AIR.defaultBlockState());
            setInChunk(chunk, minX, minZ, base.getX() - 2, base.getY() + 2, base.getZ(), Blocks.AIR.defaultBlockState());
            setInChunk(chunk, minX, minZ, base.getX() - 3, base.getY() + 1, base.getZ(), Blocks.LANTERN.defaultBlockState());
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
            else if (y < surface - 3) states[i] = Blocks.STONE.defaultBlockState();
            else if (y < surface) states[i] = Blocks.DIRT.defaultBlockState();
            else if (y == surface) states[i] = ModBlocks.PLAINS_GRASS_BLOCK.get().defaultBlockState();
            else states[i] = Blocks.AIR.defaultBlockState();
        }
        return new NoiseColumn(minY, states);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        info.add("Under World: The Plains");
    }
}
