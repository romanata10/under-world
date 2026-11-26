package com.underworld.mod.client;

import com.underworld.mod.registry.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public final class ClientFunWorldTimeHandler {
    private static volatile long syncedDayTime = 1000L;
    private static volatile int speedMultiplier = 4;
    private static volatile boolean pendingApply = false;
    private static volatile boolean applying = false;

    private ClientFunWorldTimeHandler() {}

    public static void handleSync(long dayTime, int multiplier) {
        syncedDayTime = dayTime;
        speedMultiplier = Math.max(1, multiplier);
        pendingApply = true;

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level != null && ModDimensions.isFunWorld(level)) {
            applyInto(level);
        }
    }

    public static void applyInto(ClientLevel level) {
        pendingApply = false;
        runApplying(() -> level.setDayTime(syncedDayTime));
    }

    public static void runApplying(Runnable task) {
        applying = true;
        try {
            task.run();
        } finally {
            applying = false;
        }
    }

    public static boolean hasPendingApply() {
        return pendingApply;
    }

    public static boolean isApplying() {
        return applying;
    }

    public static int getSpeedMultiplier() {
        return speedMultiplier;
    }

    public static void reset() {
        syncedDayTime = 1000L;
        speedMultiplier = 4;
        pendingApply = false;
        applying = false;
    }
}
