package com.underworld.mod.world;

import com.underworld.mod.config.UnderWorldConfigHolder;
import com.underworld.mod.network.NetworkHandler;
import com.underworld.mod.registry.ModDimensions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class FunWorldTimeManager {
    private static volatile long serverFunWorldDayTime = 1000L;
    private static volatile WeakReference<MinecraftServer> activeServer = new WeakReference<>(null);
    private static final List<WeakReference<Object>> FUN_WORLD_LEVEL_DATA = new ArrayList<>();

    private FunWorldTimeManager() {}

    public static void registerFunWorldData(@Nullable Object levelData) {
        if (levelData == null) {
            return;
        }
        synchronized (FUN_WORLD_LEVEL_DATA) {
            Iterator<WeakReference<Object>> it = FUN_WORLD_LEVEL_DATA.iterator();
            while (it.hasNext()) {
                Object existing = it.next().get();
                if (existing == null) {
                    it.remove();
                } else if (existing == levelData) {
                    return;
                }
            }
            FUN_WORLD_LEVEL_DATA.add(new WeakReference<>(levelData));
        }
    }

    public static boolean isFunWorldData(@Nullable Object levelData) {
        if (levelData == null) {
            return false;
        }
        synchronized (FUN_WORLD_LEVEL_DATA) {
            for (int i = 0, n = FUN_WORLD_LEVEL_DATA.size(); i < n; i++) {
                if (FUN_WORLD_LEVEL_DATA.get(i).get() == levelData) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void bindServer(MinecraftServer server) {
        activeServer = new WeakReference<>(server);
        UnderWorldSavedData data = UnderWorldSavedData.get(server);
        serverFunWorldDayTime = data.getFunWorldDayTime();
    }

    public static void unbindServer() {
        activeServer = new WeakReference<>(null);
        serverFunWorldDayTime = 1000L;
        synchronized (FUN_WORLD_LEVEL_DATA) {
            FUN_WORLD_LEVEL_DATA.clear();
        }
    }

    public static long getServerDayTime() {
        return serverFunWorldDayTime;
    }

    public static int getSpeedMultiplier() {
        if (UnderWorldConfigHolder.get() == null) {
            return 4;
        }
        return Math.max(1, UnderWorldConfigHolder.get().dimensions.funWorldTimeSpeedMultiplier.get());
    }

    public static void setServerDayTime(long newTime) {
        long clamped = Math.max(0L, newTime);
        serverFunWorldDayTime = clamped;
        MinecraftServer server = activeServer.get();
        if (server != null) {
            UnderWorldSavedData.get(server).setFunWorldDayTime(clamped);
            syncToFunWorldPlayers(server);
        }
    }

    public static void tickServer(MinecraftServer server) {
        activeServer = new WeakReference<>(server);
        ServerLevel funLevel = server.getLevel(ModDimensions.THE_FUN_WORLD);
        if (funLevel == null) {
            return;
        }

        UnderWorldSavedData data = UnderWorldSavedData.get(server);
        long current = data.getFunWorldDayTime();
        int mult = getSpeedMultiplier();

        if (funLevel.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            current += mult;
            data.setFunWorldDayTime(current);
        }
        serverFunWorldDayTime = current;
        funLevel.updateSkyBrightness();

        if (server.getTickCount() % 20 == 0 && !funLevel.players().isEmpty()) {
            syncToFunWorldPlayers(server);
        }
    }

    public static void syncToPlayer(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        UnderWorldSavedData data = UnderWorldSavedData.get(server);
        serverFunWorldDayTime = data.getFunWorldDayTime();
        NetworkHandler.syncFunWorldTimeToPlayer(player, serverFunWorldDayTime, getSpeedMultiplier());
    }

    public static void syncToFunWorldPlayers(@Nullable MinecraftServer server) {
        if (server == null) {
            return;
        }
        ServerLevel funLevel = server.getLevel(ModDimensions.THE_FUN_WORLD);
        if (funLevel == null || funLevel.players().isEmpty()) {
            return;
        }
        int mult = getSpeedMultiplier();
        for (ServerPlayer player : funLevel.players()) {
            NetworkHandler.syncFunWorldTimeToPlayer(player, serverFunWorldDayTime, mult);
        }
    }
}
