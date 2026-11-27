package com.underworld.mod.world.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.underworld.mod.registry.ModBlocks;
import com.underworld.mod.world.structure.StructureBuildUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
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

public class BrowserChunkGenerator extends ChunkGenerator {
    public static final int FLOOR_Y = 64;
    public static final int CORRUPT_CENTER_X = 136;
    public static final int CORRUPT_CENTER_Z = -120;

    public static final Codec<BrowserChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
            ).apply(instance, BrowserChunkGenerator::new)
    );

    public BrowserChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    public static String getSiteId(int wx, int wz) {
        int rx = Math.floorMod(wx - CORRUPT_CENTER_X + 192, 384) - 192;
        int rz = Math.floorMod(wz - CORRUPT_CENTER_Z + 192, 384) - 192;
        if (rx * rx + rz * rz <= 34 * 34) {
            return "minecraft_free_download";
        }
        if (Math.abs(wx) <= 32 && Math.abs(wz) <= 32) {
            return "google";
        }
        if (wz < -32 && Math.abs(wx) <= 72) {
            return "youtube";
        }
        if (wx > 32 && Math.abs(wz) <= 72) {
            return "roblox";
        }
        if (wz > 32 && Math.abs(wx) <= 72) {
            return "steam";
        }
        if (wx < -32 && Math.abs(wz) <= 72) {
            return "reddit";
        }
        return ((Math.abs(wx) + Math.abs(wz)) % 128 < 64) ? "google" : "youtube";
    }

    public static String getSiteUrl(int wx, int wz) {
        return switch (getSiteId(wx, wz)) {
            case "youtube" -> "http://www.youtube.com/watch?v=000000";
            case "roblox" -> "http://www.roblox.com/games/old_place";
            case "steam" -> "http://store.steampowered.com/";
            case "reddit" -> "http://www.reddit.com/r/underworld/";
            case "minecraft_free_download" -> "http://the-MinECRaT-fREe-downladn.com/";
            default -> "http://www.google.com/";
        };
    }

    /**
     * Carves the illuminated Escape Passage to the LEFT (-X) when DOWNLOAD is triggered at runtime!
     */
    public static void activateDownloadTrap(ServerLevel level, BlockPos downloadPos, ServerPlayer triggerPlayer) {
        BlockPos escapeCenter = downloadPos.offset(-12, 0, 0);
        if (level.getBlockState(escapeCenter.above()).is(ModBlocks.BROWSER_EXIT_PORTAL.get())) {
            return;
        }
        for (int dx = -15; dx <= -4; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                level.setBlock(downloadPos.offset(dx, -1, dz), Blocks.WHITE_CONCRETE.defaultBlockState(), 3);
                level.setBlock(downloadPos.offset(dx, 0, dz), Blocks.CYAN_CONCRETE.defaultBlockState(), 3);
                for (int dy = 1; dy <= 4; dy++) {
                    level.setBlock(downloadPos.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        for (int dz = -1; dz <= 1; dz++) {
            for (int dy = 1; dy <= 3; dy++) {
                level.setBlock(escapeCenter.offset(0, dy, dz), ModBlocks.BROWSER_EXIT_PORTAL.get().defaultBlockState(), 3);
            }
        }
        for (int dy = 1; dy <= 4; dy++) {
            level.setBlock(escapeCenter.offset(0, dy, -2), Blocks.SEA_LANTERN.defaultBlockState(), 3);
            level.setBlock(escapeCenter.offset(0, dy, 2), Blocks.SEA_LANTERN.defaultBlockState(), 3);
        }
        for (int dz = -2; dz <= 2; dz++) {
            level.setBlock(escapeCenter.offset(0, 4, dz), Blocks.SEA_LANTERN.defaultBlockState(), 3);
        }
        StructureBuildUtil.placeWallOrStandingSign(level,
                new net.minecraft.world.level.levelgen.structure.BoundingBox(escapeCenter.offset(2, 1, 0)),
                escapeCenter.offset(2, 1, 0), Direction.EAST, DyeColor.CYAN, true,
                "<<< RUN LEFT", "ESCAPE PORTAL", "", "");
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
        BlockState tile = ModBlocks.BROWSER_TILE.get().defaultBlockState();
        BlockState bar = ModBlocks.BROWSER_BAR_BLOCK.get().defaultBlockState();
        BlockState yt = ModBlocks.SITE_YOUTUBE_BLOCK.get().defaultBlockState();
        BlockState rb = ModBlocks.SITE_ROBLOX_BLOCK.get().defaultBlockState();
        BlockState st = ModBlocks.SITE_STEAM_BLOCK.get().defaultBlockState();
        BlockState rd = ModBlocks.SITE_REDDIT_BLOCK.get().defaultBlockState();
        BlockState glitch = ModBlocks.SITE_GLITCH_BLOCK.get().defaultBlockState();

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int wx = minX + lx;
                int wz = minZ + lz;
                String site = getSiteId(wx, wz);

                pos.set(lx, -64, lz);
                chunk.setBlockState(pos, bedrock, false);

                BlockState floorState = switch (site) {
                    case "youtube" -> (Math.floorMod(wx + wz, 5) == 0 ? yt : tile);
                    case "roblox" -> (Math.floorMod(wx, 4) == 0 || Math.floorMod(wz, 4) == 0 ? rb : Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState());
                    case "steam" -> st;
                    case "reddit" -> (Math.floorMod(wz, 6) == 0 ? rd : tile);
                    case "minecraft_free_download" -> glitch;
                    default -> tile;
                };

                for (int y = FLOOR_Y - 4; y < FLOOR_Y; y++) {
                    pos.set(lx, y, lz);
                    chunk.setBlockState(pos, site.equals("minecraft_free_download") ? Blocks.BLACK_CONCRETE.defaultBlockState() : Blocks.WHITE_CONCRETE.defaultBlockState(), false);
                }
                pos.set(lx, FLOOR_Y, lz);
                chunk.setBlockState(pos, floorState, false);

                if (Math.floorMod(wx, 24) == 0 || Math.floorMod(wz, 24) == 0) {
                    pos.set(lx, 84, lz);
                    chunk.setBlockState(pos, site.equals("minecraft_free_download") ? Blocks.BLACK_CONCRETE.defaultBlockState() : bar, false);
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
        RandomSource rng = RandomSource.create(level.getSeed() ^ (cx * 73856093L + cz * 19349663L));

        // Google Search Spawn Hub strictly inside chunk (0, 0)
        if (cx == 0 && cz == 0) {
            BlockPos hub = new BlockPos(6, FLOOR_Y, 6);
            BlockState[] cols = {
                    Blocks.BLUE_CONCRETE.defaultBlockState(),
                    Blocks.RED_CONCRETE.defaultBlockState(),
                    Blocks.YELLOW_CONCRETE.defaultBlockState(),
                    Blocks.BLUE_CONCRETE.defaultBlockState(),
                    Blocks.LIME_CONCRETE.defaultBlockState(),
                    Blocks.RED_CONCRETE.defaultBlockState()
            };
            for (int i = 0; i < cols.length; i++) {
                setInChunk(chunk, minX, minZ, hub.getX() + i - 2, hub.getY() + 1, hub.getZ() - 4, cols[i]);
                setInChunk(chunk, minX, minZ, hub.getX() + i - 2, hub.getY() + 2, hub.getZ() - 4, cols[i]);
            }
            setInChunk(chunk, minX, minZ, hub.getX(), hub.getY() + 1, hub.getZ() + 5, ModBlocks.COMPUTER.get().defaultBlockState());
            return;
        }

        // Corrupted Download Arena strictly inside chunk (8, -8) at center (136, FLOOR_Y, -120)
        if (Math.floorMod((minX + 8) - CORRUPT_CENTER_X, 384) == 0 && Math.floorMod((minZ + 8) - CORRUPT_CENTER_Z, 384) == 0) {
            placeFreeDownloadArenaInChunk(chunk, minX, minZ, new BlockPos(minX + 8, FLOOR_Y, minZ + 8));
            return;
        }

        int mx = minX + 8;
        int mz = minZ + 8;
        String site = getSiteId(mx, mz);
        if (site.equals("youtube") && rng.nextInt(3) == 0) {
            placeSiteBannerInChunk(chunk, minX, minZ, new BlockPos(mx, FLOOR_Y, mz),
                    ModBlocks.SITE_YOUTUBE_BLOCK.get().defaultBlockState(), Blocks.RED_CONCRETE.defaultBlockState());
        } else if (site.equals("roblox") && rng.nextInt(3) == 0) {
            placeSiteBannerInChunk(chunk, minX, minZ, new BlockPos(mx, FLOOR_Y, mz),
                    ModBlocks.SITE_ROBLOX_BLOCK.get().defaultBlockState(), Blocks.RED_CONCRETE.defaultBlockState());
        } else if (site.equals("steam") && rng.nextInt(3) == 0) {
            placeSiteBannerInChunk(chunk, minX, minZ, new BlockPos(mx, FLOOR_Y, mz),
                    ModBlocks.SITE_STEAM_BLOCK.get().defaultBlockState(), Blocks.LIGHT_BLUE_CONCRETE.defaultBlockState());
        } else if (site.equals("reddit") && rng.nextInt(3) == 0) {
            placeSiteBannerInChunk(chunk, minX, minZ, new BlockPos(mx, FLOOR_Y, mz),
                    ModBlocks.SITE_REDDIT_BLOCK.get().defaultBlockState(), Blocks.ORANGE_CONCRETE.defaultBlockState());
        } else if (site.equals("minecraft_free_download") && rng.nextInt(2) == 0) {
            int h = 3 + rng.nextInt(6);
            for (int dy = 1; dy <= h; dy++) {
                setInChunk(chunk, minX, minZ, mx, FLOOR_Y + dy, mz, ModBlocks.SITE_GLITCH_BLOCK.get().defaultBlockState());
            }
        }
    }

    private static void placeSiteBannerInChunk(ChunkAccess chunk, int minX, int minZ, BlockPos base, BlockState main, BlockState accent) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = 1; dy <= 5; dy++) {
                boolean rim = Math.abs(dx) == 2 || dy == 1 || dy == 5;
                setInChunk(chunk, minX, minZ, base.getX() + dx, base.getY() + dy, base.getZ(), rim ? main : accent);
            }
        }
    }

    private static void placeFreeDownloadArenaInChunk(ChunkAccess chunk, int minX, int minZ, BlockPos base) {
        BlockState black = Blocks.BLACK_CONCRETE.defaultBlockState();
        BlockState green = Blocks.LIME_CONCRETE.defaultBlockState();
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
                setInChunk(chunk, minX, minZ, base.getX() + dx, base.getY(), base.getZ() + dz,
                        ModBlocks.SITE_GLITCH_BLOCK.get().defaultBlockState());
            }
        }
        for (int dx = -5; dx <= 5; dx++) {
            for (int dy = 3; dy <= 7; dy++) {
                boolean border = Math.abs(dx) == 5 || dy == 3 || dy == 7;
                setInChunk(chunk, minX, minZ, base.getX() + dx, base.getY() + dy, base.getZ() - 4, border ? black : green);
            }
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                setInChunk(chunk, minX, minZ, base.getX() + dx, base.getY() + 1, base.getZ() + dz, black);
            }
        }
        setInChunk(chunk, minX, minZ, base.getX(), base.getY() + 1, base.getZ(), ModBlocks.DOWNLOAD_BUTTON_BLOCK.get().defaultBlockState());
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
        return FLOOR_Y + 1;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
        int height = level.getHeight();
        BlockState[] states = new BlockState[height];
        int minY = level.getMinBuildHeight();
        for (int i = 0; i < height; i++) {
            int y = minY + i;
            if (y == -64) states[i] = Blocks.BEDROCK.defaultBlockState();
            else if (y >= FLOOR_Y - 4 && y <= FLOOR_Y) states[i] = ModBlocks.BROWSER_TILE.get().defaultBlockState();
            else states[i] = Blocks.AIR.defaultBlockState();
        }
        return new NoiseColumn(minY, states);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        info.add("Under World: The Browser (" + getSiteUrl(pos.getX(), pos.getZ()) + ")");
    }
}
