package com.underworld.mod.registry;

import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.config.UnderWorldConfigHolder;
import com.underworld.mod.event.UnderWorldEventDirector;
import com.underworld.mod.world.UnderWorldSavedData;
import com.underworld.mod.world.dimension.BrowserChunkGenerator;
import com.underworld.mod.world.dimension.ErrorChunkGenerator;
import com.underworld.mod.world.dimension.FunWorldChunkGenerator;
import com.underworld.mod.world.dimension.MazeChunkGenerator;
import com.underworld.mod.world.dimension.PlainsChunkGenerator;
import com.underworld.mod.world.dimension.TimeChunkGenerator;
import com.underworld.mod.world.dimension.WindOceanChunkGenerator;
import com.underworld.mod.world.dimension.XpDesktopChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
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
    public static final ResourceKey<Level> THE_ERROR =
            ResourceKey.create(Registries.DIMENSION, UnderWorldMod.id("the_error"));
    public static final ResourceKey<Level> THE_TIME =
            ResourceKey.create(Registries.DIMENSION, UnderWorldMod.id("the_time"));
    public static final ResourceKey<Level> THE_PLAINS =
            ResourceKey.create(Registries.DIMENSION, UnderWorldMod.id("the_plains"));
    public static final ResourceKey<Level> THE_WIND_OCEAN =
            ResourceKey.create(Registries.DIMENSION, UnderWorldMod.id("the_wind_ocean"));
    public static final ResourceKey<Level> THE_XP =
            ResourceKey.create(Registries.DIMENSION, UnderWorldMod.id("the_xp"));
    public static final ResourceKey<Level> THE_BROWSER =
            ResourceKey.create(Registries.DIMENSION, UnderWorldMod.id("the_browser"));

    public static final ResourceKey<DimensionType> THE_FUN_WORLD_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, UnderWorldMod.id("the_fun_world"));
    public static final ResourceKey<DimensionType> MAZE_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, UnderWorldMod.id("maze"));
    public static final ResourceKey<DimensionType> THE_ERROR_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, UnderWorldMod.id("the_error"));
    public static final ResourceKey<DimensionType> THE_TIME_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, UnderWorldMod.id("the_time"));
    public static final ResourceKey<DimensionType> THE_PLAINS_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, UnderWorldMod.id("the_plains"));
    public static final ResourceKey<DimensionType> THE_WIND_OCEAN_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, UnderWorldMod.id("the_wind_ocean"));
    public static final ResourceKey<DimensionType> THE_XP_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, UnderWorldMod.id("the_xp"));
    public static final ResourceKey<DimensionType> THE_BROWSER_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, UnderWorldMod.id("the_browser"));

    private ModDimensions() {}

    public static boolean isFunWorld(@Nullable Level level) {
        return level != null && level.dimension().equals(THE_FUN_WORLD);
    }

    public static boolean isMaze(@Nullable Level level) {
        return level != null && level.dimension().equals(MAZE);
    }

    public static boolean isError(@Nullable Level level) {
        return level != null && level.dimension().equals(THE_ERROR);
    }

    public static boolean isTime(@Nullable Level level) {
        return level != null && level.dimension().equals(THE_TIME);
    }

    public static boolean isPlains(@Nullable Level level) {
        return level != null && level.dimension().equals(THE_PLAINS);
    }

    public static boolean isWindOcean(@Nullable Level level) {
        return level != null && level.dimension().equals(THE_WIND_OCEAN);
    }

    public static boolean isXp(@Nullable Level level) {
        return level != null && level.dimension().equals(THE_XP);
    }

    public static boolean isBrowser(@Nullable Level level) {
        return level != null && level.dimension().equals(THE_BROWSER);
    }

    public static boolean sendToFunWorld(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null || player.isRemoved() || !player.isAlive()) {
            return false;
        }
        ServerLevel dest = server.getLevel(THE_FUN_WORLD);
        if (dest == null || player.level() == dest) {
            return false;
        }
        if (player.level().dimension().equals(Level.OVERWORLD)) {
            UnderWorldSavedData.get(server).saveReturnPos(player.getUUID(), player.blockPosition());
        }
        int spawnX = 8;
        int spawnZ = 8;
        int surfaceY = FunWorldChunkGenerator.getSurfaceY(spawnX, spawnZ);
        BlockPos landPos = new BlockPos(spawnX, surfaceY + 1, spawnZ);

        dest.getChunkAt(landPos);
        ensureClearStandArea(dest, landPos, ModBlocks.FUN_GRASS_BLOCK.get().defaultBlockState());

        player.stopRiding();
        player.fallDistance = 0.0F;
        player.changeDimension(dest, fixedTeleporter(new Vec3(landPos.getX() + 0.5D, landPos.getY(), landPos.getZ() + 0.5D), player.getYRot(), player.getXRot()));
        dest.playSound(null, landPos, ModSounds.FUN_WORLD_WARP.get(), SoundSource.AMBIENT, 1.4F, 1.0F);
        player.displayClientMessage(Component.translatable("message.under_world.fun_world_escape_hint"), true);
        return true;
    }

    public static boolean sendToMaze(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null || player.isRemoved() || !player.isAlive()) {
            return false;
        }
        ServerLevel dest = server.getLevel(MAZE);
        if (dest == null || player.level() == dest) {
            return false;
        }
        if (player.level().dimension().equals(Level.OVERWORLD)) {
            UnderWorldSavedData.get(server).saveReturnPos(player.getUUID(), player.blockPosition());
        }
        BlockPos landPos = new BlockPos(10, MazeChunkGenerator.FLOOR_Y + 1, 10);
        dest.getChunkAt(landPos);

        player.stopRiding();
        player.fallDistance = 0.0F;
        player.changeDimension(dest, fixedTeleporter(new Vec3(landPos.getX() + 0.5D, landPos.getY(), landPos.getZ() + 0.5D), player.getYRot(), 0.0F));
        dest.playSound(null, landPos, ModSounds.MAZE_PORTAL.get(), SoundSource.AMBIENT, 1.4F, 0.9F);
        return true;
    }

    public static boolean sendToError(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null || player.isRemoved() || !player.isAlive()) {
            return false;
        }
        ServerLevel dest = server.getLevel(THE_ERROR);
        if (dest == null || player.level() == dest) {
            return false;
        }
        UnderWorldSavedData data = UnderWorldSavedData.get(server);
        if (player.level().dimension().equals(Level.OVERWORLD)) {
            data.saveReturnPos(player.getUUID(), player.blockPosition());
        }
        int duration = UnderWorldConfigHolder.getErrorStayDurationTicks();
        data.setErrorReturnTimer(player.getUUID(), duration);

        int spawnX = 8;
        int spawnZ = 8;
        int surfaceY = ErrorChunkGenerator.getSurfaceYAt(spawnX, spawnZ);
        if (surfaceY <= -64) {
            surfaceY = ErrorChunkGenerator.getBaseSurfaceY(spawnX, spawnZ);
        }
        BlockPos landPos = new BlockPos(spawnX, surfaceY + 1, spawnZ);
        dest.getChunkAt(landPos);
        ensureClearStandArea(dest, landPos, ModBlocks.ERROR_GROUND.get().defaultBlockState());

        player.stopRiding();
        player.fallDistance = 0.0F;
        player.changeDimension(dest, fixedTeleporter(new Vec3(landPos.getX() + 0.5D, landPos.getY(), landPos.getZ() + 0.5D), player.getYRot(), player.getXRot()));
        dest.playSound(null, landPos, ModSounds.ERROR_WARP.get(), SoundSource.AMBIENT, 1.35F, 1.0F);
        player.displayClientMessage(Component.translatable("message.under_world.error_entered"), true);
        return true;
    }

    public static boolean sendToTime(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null || player.isRemoved() || !player.isAlive()) {
            return false;
        }
        ServerLevel dest = server.getLevel(THE_TIME);
        if (dest == null || player.level() == dest) {
            return false;
        }
        if (player.level().dimension().equals(Level.OVERWORLD)) {
            UnderWorldSavedData.get(server).saveReturnPos(player.getUUID(), player.blockPosition());
        }
        BlockPos landPos = new BlockPos(8, TimeChunkGenerator.BASE_DISC_Y + 1, 8);
        dest.getChunkAt(landPos);
        ensureClearStandArea(dest, landPos, ModBlocks.CLOCK_FACE_BLOCK.get().defaultBlockState());

        player.stopRiding();
        player.fallDistance = 0.0F;
        player.changeDimension(dest, fixedTeleporter(new Vec3(landPos.getX() + 0.5D, landPos.getY(), landPos.getZ() + 0.5D), player.getYRot(), 0.0F));
        dest.playSound(null, landPos, ModSounds.TIME_WARP.get(), SoundSource.AMBIENT, 1.35F, 0.95F);
        player.displayClientMessage(Component.translatable("message.under_world.time_entered"), true);
        return true;
    }

    public static boolean returnToOverworld(ServerPlayer player) {
        return returnToOverworldWithEffect(player, Component.translatable("message.under_world.maze_escape"),
                ModSounds.MAZE_PORTAL.get(), 1.1F, 1.15F);
    }

    public static boolean returnFromError(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server != null) {
            UnderWorldSavedData.get(server).clearErrorReturnTimer(player.getUUID());
        }
        return returnToOverworldWithEffect(player, Component.translatable("message.under_world.error_escape"),
                ModSounds.ERROR_WARP.get(), 1.2F, 1.1F);
    }

    public static boolean returnToOverworldFromTime(ServerPlayer player) {
        return returnToOverworldWithEffect(player, Component.translatable("message.under_world.time_escape"),
                ModSounds.TIME_WARP.get(), 1.25F, 1.1F);
    }

    public static boolean sendToPlains(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null || player.isRemoved() || !player.isAlive()) return false;
        ServerLevel dest = server.getLevel(THE_PLAINS);
        if (dest == null || player.level() == dest) return false;
        if (player.level().dimension().equals(Level.OVERWORLD)) {
            UnderWorldSavedData.get(server).saveReturnPos(player.getUUID(), player.blockPosition());
        }
        int spawnX = 8;
        int spawnZ = PlainsChunkGenerator.getPathZ(spawnX);
        int surfaceY = PlainsChunkGenerator.getSurfaceY(spawnX, spawnZ);
        BlockPos landPos = new BlockPos(spawnX, surfaceY + 1, spawnZ);
        dest.getChunkAt(landPos);
        ensureClearStandArea(dest, landPos, ModBlocks.PLAINS_PATH_BLOCK.get().defaultBlockState());

        player.stopRiding();
        player.fallDistance = 0.0F;
        player.changeDimension(dest, fixedTeleporter(new Vec3(landPos.getX() + 0.5D, landPos.getY(), landPos.getZ() + 0.5D), -90.0F, 0.0F));
        dest.playSound(null, landPos, ModSounds.PYLON_HUM.get(), SoundSource.AMBIENT, 1.35F, 0.9F);
        player.displayClientMessage(Component.translatable("message.under_world.plains_entered"), true);
        return true;
    }

    public static boolean sendToWindOcean(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null || player.isRemoved() || !player.isAlive()) return false;
        ServerLevel dest = server.getLevel(THE_WIND_OCEAN);
        if (dest == null || player.level() == dest) return false;
        if (player.level().dimension().equals(Level.OVERWORLD)) {
            UnderWorldSavedData.get(server).saveReturnPos(player.getUUID(), player.blockPosition());
        }
        BlockPos boatPos = new BlockPos(8, WindOceanChunkGenerator.SEA_LEVEL, 8);
        dest.getChunkAt(boatPos);

        player.stopRiding();
        player.fallDistance = 0.0F;
        Entity moved = player.changeDimension(dest, fixedTeleporter(
                new Vec3(boatPos.getX() + 0.5D, WindOceanChunkGenerator.SEA_LEVEL + 0.85D, boatPos.getZ() + 0.5D), -90.0F, 0.0F));
        if (moved instanceof ServerPlayer destPlayer) {
            java.util.UUID pid = destPlayer.getUUID();
            UnderWorldEventDirector.schedule(2, () -> {
                ServerPlayer p = server.getPlayerList().getPlayer(pid);
                if (p != null && p.isAlive() && p.level() == dest) {
                    Boat boat = EntityType.BOAT.create(dest);
                    if (boat != null) {
                        boat.setVariant(Boat.Type.OAK);
                        boat.moveTo(p.getX(), WindOceanChunkGenerator.SEA_LEVEL + 0.25D, p.getZ(), p.getYRot(), 0.0F);
                        dest.addFreshEntity(boat);
                        p.startRiding(boat, true);
                    }
                    dest.playSound(null, p.blockPosition(), ModSounds.TURBINE_WHOOSH.get(), SoundSource.AMBIENT, 1.4F, 0.85F);
                    p.displayClientMessage(Component.translatable("message.under_world.wind_ocean_entered"), true);
                }
            });
        }
        return true;
    }

    public static boolean sendToXp(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null || player.isRemoved() || !player.isAlive()) return false;
        ServerLevel dest = server.getLevel(THE_XP);
        if (dest == null || player.level() == dest) return false;
        if (player.level().dimension().equals(Level.OVERWORLD)) {
            UnderWorldSavedData.get(server).saveReturnPos(player.getUUID(), player.blockPosition());
        }
        BlockPos landPos = new BlockPos(0, XpDesktopChunkGenerator.DESKTOP_Y + 1, 6);
        dest.getChunkAt(landPos);
        ensureClearStandArea(dest, landPos, Blocks.LIGHT_BLUE_CONCRETE.defaultBlockState());

        player.stopRiding();
        player.fallDistance = 0.0F;
        player.changeDimension(dest, fixedTeleporter(new Vec3(landPos.getX() + 0.5D, landPos.getY(), landPos.getZ() + 0.5D), 180.0F, 0.0F));
        dest.playSound(null, landPos, ModSounds.XP_STARTUP.get(), SoundSource.AMBIENT, 1.4F, 1.0F);
        player.displayClientMessage(Component.translatable("message.under_world.xp_entered"), true);
        return true;
    }

    public static boolean sendToBrowser(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null || player.isRemoved() || !player.isAlive()) return false;
        ServerLevel dest = server.getLevel(THE_BROWSER);
        if (dest == null || player.level() == dest) return false;
        if (player.level().dimension().equals(Level.OVERWORLD)) {
            UnderWorldSavedData.get(server).saveReturnPos(player.getUUID(), player.blockPosition());
        }
        BlockPos landPos = new BlockPos(6, BrowserChunkGenerator.FLOOR_Y + 1, 8);
        dest.getChunkAt(landPos);
        ensureClearStandArea(dest, landPos, ModBlocks.BROWSER_TILE.get().defaultBlockState());

        player.stopRiding();
        player.fallDistance = 0.0F;
        player.changeDimension(dest, fixedTeleporter(new Vec3(landPos.getX() + 0.5D, landPos.getY(), landPos.getZ() + 0.5D), 180.0F, 0.0F));
        dest.playSound(null, landPos, ModSounds.XP_STARTUP.get(), SoundSource.AMBIENT, 1.25F, 1.15F);
        player.displayClientMessage(Component.translatable("message.under_world.browser_entered"), true);
        return true;
    }

    public static boolean returnToOverworldFromNewDim(ServerPlayer player, String messageKey) {
        return returnToOverworldWithEffect(player, Component.translatable(messageKey),
                ModSounds.XP_STARTUP.get(), 1.2F, 1.0F);
    }

    private static boolean returnToOverworldWithEffect(ServerPlayer player, Component message,
                                                       SoundEvent sound, float volume, float pitch) {
        MinecraftServer server = player.getServer();
        if (server == null || player.isRemoved() || !player.isAlive()) {
            return false;
        }
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null || player.level() == overworld) {
            return false;
        }
        UnderWorldSavedData data = UnderWorldSavedData.get(server);
        data.clearErrorReturnTimer(player.getUUID());
        BlockPos saved = data.getReturnPos(player.getUUID());
        BlockPos target = saved != null ? saved : overworld.getSharedSpawnPos();
        overworld.getChunkAt(target);

        BlockPos safeTarget = target;
        if (!overworld.getBlockState(safeTarget).isAir() || !overworld.getBlockState(safeTarget.above()).isAir()) {
            int hy = overworld.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, target).getY();
            safeTarget = new BlockPos(target.getX(), Math.max(overworld.getMinBuildHeight() + 4, Math.min(overworld.getMaxBuildHeight() - 4, hy)), target.getZ());
        }

        player.stopRiding();
        player.fallDistance = 0.0F;
        player.changeDimension(overworld, fixedTeleporter(new Vec3(safeTarget.getX() + 0.5D, safeTarget.getY(), safeTarget.getZ() + 0.5D), player.getYRot(), player.getXRot()));
        overworld.playSound(null, safeTarget, sound, SoundSource.AMBIENT, volume, pitch);
        player.displayClientMessage(message, false);
        return true;
    }

    private static void ensureClearStandArea(ServerLevel level, BlockPos pos, net.minecraft.world.level.block.state.BlockState floorBlock) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos below = pos.offset(dx, -1, dz);
                if (level.getBlockState(below).isAir()) {
                    level.setBlock(below, floorBlock, 3);
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
