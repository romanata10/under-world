package com.underworld.mod.world.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.underworld.mod.registry.ModBlocks;
import com.underworld.mod.world.structure.StructureBuildUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.IronBarsBlock;
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

        // Smooth gentle hill rise around distant wind-turbine landmarks (x = 416 + 512*k)
        int modX = Math.floorMod(wx, 512);
        double dxHill = Math.abs(modX - 416);
        double dzHill = Math.abs(wz);
        double hillDist = Math.hypot(dxHill, dzHill);
        double turbineHill = hillDist < 52.0D ? (1.0D - hillDist / 52.0D) * 14.0D : 0.0D;

        // Keep winding footpath smooth
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

        // Return beacon near spawn (x=8, z=pathZ(8))
        if (cx == 0 && Math.abs(minZ - getPathZ(8)) <= 16) {
            int rz = getPathZ(8) + 4;
            int ry = getSurfaceY(8, rz);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    level.setBlock(new BlockPos(8 + dx, ry, rz + dz), ModBlocks.PLAINS_PORTAL_GRASS.get().defaultBlockState(), 2);
                }
            }
        }

        // Sparse wild tall grass along rolling meadow hills
        for (int i = 0; i < 16; i++) {
            int wx = minX + rng.nextInt(16);
            int wz = minZ + rng.nextInt(16);
            if (Math.abs(wz - getPathZ(wx)) <= 3) continue;
            int y = getSurfaceY(wx, wz);
            BlockPos top = new BlockPos(wx, y + 1, wz);
            if (level.getBlockState(top).isAir()) {
                level.setBlock(top, Blocks.GRASS.defaultBlockState(), 2);
            }
        }

        // Power pylons along the road every 3 chunks (~48 blocks)
        if (Math.floorMod(cx, 3) == 1) {
            int px = minX + 8;
            int side = (Math.floorMod(cx / 3, 2) == 0) ? -10 : 10;
            int pz = getPathZ(px) + side;
            if (pz >= minZ && pz < minZ + 16) {
                placeStaticPylon(level, new BlockPos(px, getSurfaceY(px, pz), pz));
            }
        }

        // Abandoned houses & apartment blocks (многоэтажки) periodically along the road
        if (Math.floorMod(cx, 5) == 3 && Math.floorMod(cx, 32) != 26) {
            int bx = minX + 6;
            int side = (Math.floorMod(cx / 5, 2) == 0) ? 14 : -14;
            int bz = getPathZ(bx) + side;
            if (bz >= minZ && bz < minZ + 16) {
                int by = getSurfaceY(bx, bz);
                if (Math.floorMod(cx, 10) == 8) {
                    placeApartmentBlock(level, new BlockPos(bx, by, bz), rng);
                } else {
                    placeAbandonedHouse(level, new BlockPos(bx, by, bz), rng);
                }
            }
        }

        // Distant Wind Turbines atop hills (x = 416 + 512*k) -> Portal to The Wind Ocean!
        if (Math.floorMod(cx, 32) == 26 && cz == 0) {
            int hx = minX + 4;
            placeDistantWindTurbine(level, new BlockPos(hx, getSurfaceY(hx, 0), 0), true);
            placeDistantWindTurbine(level, new BlockPos(hx + 22, getSurfaceY(hx + 22, -18), -18), false);
            placeDistantWindTurbine(level, new BlockPos(hx + 26, getSurfaceY(hx + 26, 20), 20), false);
        }
    }

    private static void placeStaticPylon(WorldGenLevel level, BlockPos base) {
        BlockState steel = ModBlocks.PYLON_STEEL.get().defaultBlockState();
        BlockState bar = Blocks.IRON_BARS.defaultBlockState();
        // 4 splayed legs y=0..6
        for (int dy = 0; dy <= 5; dy++) {
            int r = dy < 3 ? 2 : 1;
            setSafe(level, base.offset(-r, dy, -r), steel);
            setSafe(level, base.offset(r, dy, -r), steel);
            setSafe(level, base.offset(-r, dy, r), steel);
            setSafe(level, base.offset(r, dy, r), steel);
        }
        // Central lattice mast y=6..18
        for (int dy = 6; dy <= 17; dy++) {
            setSafe(level, base.offset(0, dy, 0), steel);
            if (dy % 3 == 0) {
                setSafe(level, base.offset(1, dy, 0), bar);
                setSafe(level, base.offset(-1, dy, 0), bar);
            }
        }
        // Two wide cross-arms with drooping torn wires
        for (int dx = -4; dx <= 4; dx++) {
            setSafe(level, base.offset(dx, 12, 0), steel);
        }
        for (int dx = -3; dx <= 3; dx++) {
            setSafe(level, base.offset(dx, 16, 0), steel);
        }
        setSafe(level, base.offset(-4, 11, 0), Blocks.CHAIN.defaultBlockState());
        setSafe(level, base.offset(-4, 10, 0), Blocks.CHAIN.defaultBlockState());
        setSafe(level, base.offset(4, 11, 0), Blocks.CHAIN.defaultBlockState());
        setSafe(level, base.offset(4, 10, 0), Blocks.CHAIN.defaultBlockState());
        setSafe(level, base.offset(0, 18, 0), Blocks.REDSTONE_TORCH.defaultBlockState());
    }

    private static void placeAbandonedHouse(WorldGenLevel level, BlockPos base, RandomSource rng) {
        BlockState wall = Blocks.SPRUCE_PLANKS.defaultBlockState();
        BlockState pillar = Blocks.SPRUCE_LOG.defaultBlockState();
        BlockState floor = Blocks.COBBLESTONE.defaultBlockState();
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                setSafe(level, base.offset(dx, -1, dz), Blocks.DIRT.defaultBlockState());
                setSafe(level, base.offset(dx, 0, dz), floor);
                for (int dy = 1; dy <= 5; dy++) {
                    boolean isEdge = Math.abs(dx) == 4 || Math.abs(dz) == 4;
                    if (dy == 5) {
                        setSafe(level, base.offset(dx, dy, dz), wall);
                    } else if (isEdge) {
                        boolean isCorner = Math.abs(dx) == 4 && Math.abs(dz) == 4;
                        boolean isDoor = dx == 0 && dz == -4 && (dy == 1 || dy == 2);
                        boolean isWindow = (Math.abs(dx) == 2 || Math.abs(dz) == 2) && dy == 2;
                        if (isDoor) {
                            setSafe(level, base.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
                        } else if (isWindow) {
                            setSafe(level, base.offset(dx, dy, dz), rng.nextBoolean() ? Blocks.GLASS_PANE.defaultBlockState() : Blocks.AIR.defaultBlockState());
                        } else {
                            setSafe(level, base.offset(dx, dy, dz), isCorner ? pillar : wall);
                        }
                    } else {
                        setSafe(level, base.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        setSafe(level, base.offset(2, 1, 2), Blocks.COBWEB.defaultBlockState());
        setSafe(level, base.offset(-2, 1, 2), Blocks.CRAFTING_TABLE.defaultBlockState());
    }

    private static void placeApartmentBlock(WorldGenLevel level, BlockPos base, RandomSource rng) {
        // Multi-story weathered panel building (многоэтажка, 11x9x19 high)
        BlockState concrete = Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
        BlockState trim = Blocks.GRAY_CONCRETE.defaultBlockState();
        BlockState floor = Blocks.STONE_BRICKS.defaultBlockState();
        int floors = 4;
        for (int fl = 0; fl < floors; fl++) {
            int y0 = fl * 4;
            for (int dx = -5; dx <= 5; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    setSafe(level, base.offset(dx, y0, dz), floor);
                    for (int dy = 1; dy <= 3; dy++) {
                        boolean wall = Math.abs(dx) == 5 || Math.abs(dz) == 4;
                        boolean doorway = fl == 0 && dx == 0 && dz == -4 && dy <= 2;
                        boolean window = wall && dy == 2 && (Math.abs(dx) == 2 || Math.abs(dz) == 2);
                        if (doorway) {
                            setSafe(level, base.offset(dx, y0 + dy, dz), Blocks.AIR.defaultBlockState());
                        } else if (window) {
                            setSafe(level, base.offset(dx, y0 + dy, dz), rng.nextFloat() < 0.45F ? Blocks.GLASS_PANE.defaultBlockState() : Blocks.AIR.defaultBlockState());
                        } else if (wall) {
                            boolean corner = Math.abs(dx) == 5 && Math.abs(dz) == 4;
                            setSafe(level, base.offset(dx, y0 + dy, dz), corner ? trim : concrete);
                        } else {
                            // Open stairwell column in back right corner
                            boolean stairWell = dx >= 2 && dz >= 1;
                            if (!stairWell || dy < 3) {
                                setSafe(level, base.offset(dx, y0 + dy, dz), Blocks.AIR.defaultBlockState());
                            }
                        }
                    }
                }
            }
            // Staircase step up
            setSafe(level, base.offset(3, y0 + 1, 2), Blocks.STONE_SLAB.defaultBlockState());
            setSafe(level, base.offset(3, y0 + 2, 1), Blocks.STONE_BRICKS.defaultBlockState());
            setSafe(level, base.offset(2, y0 + 3, 1), Blocks.STONE_SLAB.defaultBlockState());
        }
        // Rooftop rim + red antenna mast
        int roofY = floors * 4;
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                setSafe(level, base.offset(dx, roofY, dz), trim);
                if (Math.abs(dx) == 5 || Math.abs(dz) == 4) {
                    setSafe(level, base.offset(dx, roofY + 1, dz), concrete);
                }
            }
        }
        for (int dy = 1; dy <= 5; dy++) {
            setSafe(level, base.offset(0, roofY + dy, 0), Blocks.IRON_BARS.defaultBlockState());
        }
        setSafe(level, base.offset(0, roofY + 6, 0), Blocks.REDSTONE_TORCH.defaultBlockState());
    }

    private static void placeDistantWindTurbine(WorldGenLevel level, BlockPos base, boolean withPortal) {
        BlockState pillar = ModBlocks.TURBINE_PILLAR.get().defaultBlockState();
        BlockState blade = ModBlocks.TURBINE_BLADE.get().defaultBlockState();
        // Foundation plinth
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                setSafe(level, base.offset(dx, 0, dz), Blocks.SMOOTH_STONE.defaultBlockState());
                setSafe(level, base.offset(dx, -1, dz), Blocks.STONE.defaultBlockState());
            }
        }
        // Tall white turbine shaft y=1..22
        for (int dy = 1; dy <= 22; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (Math.abs(dx) == 1 && Math.abs(dz) == 1) continue;
                    setSafe(level, base.offset(dx, dy, dz), pillar);
                }
            }
        }
        // Nacelle + 3 rotor blades pointing west (-X) toward the incoming road
        for (int dx = -3; dx <= 2; dx++) {
            for (int dy = 21; dy <= 23; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    setSafe(level, base.offset(dx, dy, dz), pillar);
                }
            }
        }
        // 3 sweeping blades
        for (int i = 1; i <= 11; i++) {
            setSafe(level, base.offset(-3, 22 + i, 0), blade);
            setSafe(level, base.offset(-3, 22 - i / 2, i), blade);
            setSafe(level, base.offset(-3, 22 - i / 2, -i), blade);
        }
        setSafe(level, base.offset(2, 24, 0), Blocks.REDSTONE_TORCH.defaultBlockState());

        if (withPortal) {
            // Access entrance door & Wind Ocean portal core!
            for (int dy = 1; dy <= 3; dy++) {
                setSafe(level, base.offset(-1, dy, 0), Blocks.AIR.defaultBlockState());
                setSafe(level, base.offset(0, dy, 0), ModBlocks.WIND_TURBINE_PORTAL.get().defaultBlockState());
            }
            setSafe(level, base.offset(-2, 1, 0), Blocks.AIR.defaultBlockState());
            setSafe(level, base.offset(-2, 2, 0), Blocks.AIR.defaultBlockState());
            setSafe(level, base.offset(-3, 1, 0), Blocks.LANTERN.defaultBlockState());
            StructureBuildUtil.placeWallOrStandingSign(level, base.offset(-3, 1, -1), Direction.WEST, false,
                    "sign.under_world.wind_turbine_gate.1", "sign.under_world.wind_turbine_gate.2", "", "");
        }
    }

    private static void setSafe(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (pos.getY() >= level.getMinBuildHeight() && pos.getY() < level.getMaxBuildHeight()) {
            level.setBlock(pos, state, 2);
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
