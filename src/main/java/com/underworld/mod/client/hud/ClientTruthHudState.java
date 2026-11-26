package com.underworld.mod.client.hud;

public final class ClientTruthHudState {
    private static volatile boolean active = false;
    private static volatile int syncedRemainingTicks = 0;
    private static volatile int totalTicks = 600;
    private static volatile long lastSyncMs = 0L;

    private ClientTruthHudState() {}

    public static void applySync(boolean isActive, int remainingTicks, int total) {
        active = isActive && remainingTicks > 0;
        syncedRemainingTicks = Math.max(0, remainingTicks);
        totalTicks = Math.max(20, total);
        lastSyncMs = System.currentTimeMillis();
    }

    public static void reset() {
        active = false;
        syncedRemainingTicks = 0;
        totalTicks = 600;
        lastSyncMs = 0L;
    }

    public static boolean isActive() {
        if (!active) {
            return false;
        }
        return getRemainingSeconds() > 0.0D;
    }

    public static double getRemainingSeconds() {
        if (!active) {
            return 0.0D;
        }
        long elapsedMs = Math.max(0L, System.currentTimeMillis() - lastSyncMs);
        double baseSec = syncedRemainingTicks / 20.0D;
        return Math.max(0.0D, baseSec - (elapsedMs / 1000.0D));
    }

    public static int getTotalTicks() {
        return totalTicks;
    }
}
