package com.underworld.mod.registry;

import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.world.UnderWorldSavedData;
import com.underworld.mod.world.dimension.FunWorldChunkGenerator;
import com.underworld.mod.world.dimension.MazeChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import javax.annotation.Nullable;
import java.util.function.Function;

public final class ModDimensions {
    public static final ResourceKey<Level> THE_FUN_WORLD =
            ResourceKey.create(Registries.DIMENSION, UnderWorldMod.id("the_fun_world"));
    public static final ResourceKey<Level> MAZE =
            ResourceKey.create(Registries.DIMENSION, UnderWorldMod.id("maze"));

    public static final ResourceKey<DimensionType> THE_FUN_WORLD_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, UnderWorldMod.id("the_fun_world"));
    public static final ResourceKey<DimensionType> MAZE_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, UnderWorldMod.id("maze"));

    private ModDimensions() {}

    public static boolean isFunWorld(@Nullable Level level) {
        return level != null && level.dimension().equals(THE_FUN_WORLD);
    }

    public static boolean isMaze(@Nullable Level level) {
        return level != null && level.dimension().equals(MAZE);
    }

    public static boolean sendToFunWorld(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return false;
        }
        ServerLevel dest = server.getLevel(THE_FUN_WORLD);
        if (dest == null) {
            return false;
        }
        if (player.level().dimension().equals(Level.OVERWORLD)) {
            UnderWorldSavedData data = UnderWorldSavedData.get(server);
            data.saveReturnPos(player.getUUID(), player.blockPosition());
        }
        int spawnX = 8;
        int spawnZ = 8;
        int surfaceY = FunWorldChunkGenerator.getSurfaceY(spawnX, spawnZ);
        BlockPos landPos = new BlockPos(spawnX, surfaceY + 1, spawnZ);

        // Ensure chunks loaded & safe clearing
        dest.getChunk(landPos);
        ensureClearStandArea(dest, landPos);

        player.changeDimension(dest, fixedTeleporter(new Vec3(landPos.getX() + 0.5D, landPos.getY(), landPos.getZ() + 0.5D), player.getYRot(), player.getXRot()));
        dest.playSound(null, landPos, ModSounds.FUN_WORLD_WARP.get(), SoundSource.AMBIENT, 1.4F, 1.0F);
        player.displayClientMessage(Component.translatable("message.under_world.fun_world_escape_hint"), true);
        return true;
    }

    public static boolean sendToMaze(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return false;
        }
        ServerLevel dest = server.getLevel(MAZE);
        if (dest == null) {
            return false;
        }
        if (player.level().dimension().equals(Level.OVERWORLD)) {
            UnderWorldSavedData data = UnderWorldSavedData.get(server);
            data.saveReturnPos(player.getUUID(), player.blockPosition());
        }
        // Cell (1, 1) center: wx=10, wz=10, floor y=64 -> stand y=65
        BlockPos landPos = new BlockPos(10, MazeChunkGenerator.FLOOR_Y + 1, 10);
        dest.getChunk(landPos);

        player.changeDimension(dest, fixedTeleporter(new Vec3(landPos.getX() + 0.5D, landPos.getY(), landPos.getZ() + 0.5D), player.getYRot(), 0.0F));
        dest.playSound(null, landPos, ModSounds.MAZE_PORTAL.get(), SoundSource.AMBIENT, 1.4F, 0.9F);
        return true;
    }

    public static boolean returnToOverworld(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return false;
        }
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return false;
        }
        UnderWorldSavedData data = UnderWorldSavedData.get(server);
        BlockPos saved = data.getReturnPos(player.getUUID());
        BlockPos target = saved != null ? saved : overworld.getSharedSpawnPos();
        overworld.getChunk(target);

        BlockPos safeTarget = target;
        if (!overworld.getBlockState(safeTarget).isAir() || !overworld.getBlockState(safeTarget.above()).isAir()) {
            safeTarget = new BlockPos(target.getX(), Math.max(overworld.getMinBuildHeight() + 4,
                    overworld.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, target).getY()), target.getZ());
        }

        player.changeDimension(overworld, fixedTeleporter(new Vec3(safeTarget.getX() + 0.5D, safeTarget.getY(), safeTarget.getZ() + 0.5D), player.getYRot(), player.getXRot()));
        overworld.playSound(null, safeTarget, ModSounds.MAZE_PORTAL.get(), SoundSource.AMBIENT, 1.1F, 1.15F);
        player.displayClientMessage(Component.translatable("message.under_world.maze_escape"), false);
        return true;
    }

    private static void ensureClearStandArea(ServerLevel level, BlockPos pos) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos below = pos.offset(dx, -1, dz);
                if (level.getBlockState(below).isAir()) {
                    level.setBlock(below, ModBlocks.FUN_GRASS_BLOCK.get().defaultBlockState(), 3);
                }
                for (int dy = 0; dy <= 2; dy++) {
                    BlockPos clear = pos.offset(dx, dy, dz);
                    if (!level.getBlockState(clear).isAir()) {
                        level.setBlock(clear, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private static ITeleporter fixedTeleporter(Vec3 pos, float yaw, float pitch) {
        return new ITeleporter() {
            @Override
            public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float entityYaw,
                                      Function<Boolean, Entity> repositionEntity) {
                return repositionEntity.apply(false);
            }

            @Override
            public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld,
                                            Function<ServerLevel, PortalInfo> defaultPortalInfo) {
                return new PortalInfo(pos, Vec3.ZERO, yaw, pitch);
            }

            @Override
            public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
                return false;
            }
        };
    }
}
