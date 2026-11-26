package com.underworld.mod.config;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.concurrent.atomic.AtomicBoolean;

public final class UnderWorldConfigHolder {
    private static UnderWorldConfig fzzyConfig;
    private static final AtomicBoolean SYNCING = new AtomicBoolean(false);

    private UnderWorldConfigHolder() {}

    public static void init(IEventBus modEventBus) {
        fzzyConfig = ConfigApiJava.registerAndLoadConfig(UnderWorldConfig::new, RegisterType.BOTH);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, UnderWorldForgeConfig.COMMON_SPEC, "under_world-common.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, UnderWorldForgeConfig.CLIENT_SPEC, "under_world-client.toml");

        modEventBus.addListener(UnderWorldConfigHolder::onModConfigLoad);
        modEventBus.addListener(UnderWorldConfigHolder::onModConfigReload);
    }

    public static UnderWorldConfig get() {
        return fzzyConfig;
    }

    private static void onModConfigLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == UnderWorldForgeConfig.COMMON_SPEC) {
            syncFzzyToForge();
        }
    }

    private static void onModConfigReload(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == UnderWorldForgeConfig.COMMON_SPEC) {
            syncForgeToFzzy();
        }
    }

    public static void syncFzzyToForge() {
        if (fzzyConfig == null || !UnderWorldForgeConfig.COMMON_SPEC.isLoaded()) {
            return;
        }
        if (!SYNCING.compareAndSet(false, true)) {
            return;
        }
        try {
            UnderWorldForgeConfig.Common c = UnderWorldForgeConfig.COMMON;
            c.modEnabled.set(fzzyConfig.general.modEnabled.get());
            c.spawnMinDistance.set(fzzyConfig.general.spawnMinDistance.get());
            c.spawnMaxDistance.set(fzzyConfig.general.spawnMaxDistance.get());
            c.debugLogging.set(fzzyConfig.general.debugLogging.get());

            c.friendEnabled.set(fzzyConfig.friend.friendEnabled.get());
            c.friendEveningTime.set(fzzyConfig.friend.friendEveningTime.get());
            c.friendDisplayName.set(fzzyConfig.friend.friendDisplayName.get());
            c.friendJoinBroadcast.set(fzzyConfig.friend.friendJoinBroadcast.get());
            c.friendAmbientChatEnabled.set(fzzyConfig.friend.friendAmbientChatEnabled.get());
            c.friendAmbientChatIntervalTicks.set(fzzyConfig.friend.friendAmbientChatIntervalTicks.get());
            c.friendChatResponsesEnabled.set(fzzyConfig.friend.friendChatResponsesEnabled.get());
            c.friendProfanityPunishmentEnabled.set(fzzyConfig.friend.friendProfanityPunishmentEnabled.get());
            c.friendStalkerEnabled.set(fzzyConfig.friend.friendStalkerEnabled.get());
            c.friendFunWorldWarpChance.set(fzzyConfig.friend.friendFunWorldWarpChance.get());
            c.friendDoorInteractionEnabled.set(fzzyConfig.friend.friendDoorInteractionEnabled.get());

            c.handsEnabled.set(fzzyConfig.hands.handsEnabled.get());
            c.handsCaveSpawnChance.set(fzzyConfig.hands.handsCaveSpawnChance.get());
            c.handsChaseDurationTicks.set(fzzyConfig.hands.handsChaseDurationTicks.get());
            c.handsBreakBlocks.set(fzzyConfig.hands.handsBreakBlocks.get());
            c.handsKickOnKill.set(fzzyConfig.hands.handsKickOnKill.get());

            c.treeMimicEnabled.set(fzzyConfig.treeMimic.treeMimicEnabled.get());
            c.treeMimicSpawnChance.set(fzzyConfig.treeMimic.treeMimicSpawnChance.get());
            c.treeMimicTriggerRange.set(fzzyConfig.treeMimic.treeMimicTriggerRange.get());
            c.treeMimicRequireAxe.set(fzzyConfig.treeMimic.treeMimicRequireAxe.get());

            c.theSkyEnabled.set(fzzyConfig.theSky.theSkyEnabled.get());
            c.theSkySpawnChance.set(fzzyConfig.theSky.theSkySpawnChance.get());
            c.theSkyVanishRange.set(fzzyConfig.theSky.theSkyVanishRange.get());
            c.theSkyStompChance.set(fzzyConfig.theSky.theSkyStompChance.get());

            c.funWorldEnabled.set(fzzyConfig.dimensions.funWorldEnabled.get());
            c.eyesOfFunEnabled.set(fzzyConfig.dimensions.eyesOfFunEnabled.get());
            c.eyesOfFunInstantKill.set(fzzyConfig.dimensions.eyesOfFunInstantKill.get());
            c.mazeEnabled.set(fzzyConfig.dimensions.mazeEnabled.get());
            c.smileSprintSpeed.set(fzzyConfig.dimensions.smileSprintSpeed.get());

            UnderWorldForgeConfig.COMMON_SPEC.save();
        } finally {
            SYNCING.set(false);
        }
    }

    public static void syncForgeToFzzy() {
        if (fzzyConfig == null || !UnderWorldForgeConfig.COMMON_SPEC.isLoaded()) {
            return;
        }
        if (!SYNCING.compareAndSet(false, true)) {
            return;
        }
        try {
            UnderWorldForgeConfig.Common c = UnderWorldForgeConfig.COMMON;
            fzzyConfig.general.modEnabled.trySet(c.modEnabled.get());
            fzzyConfig.general.spawnMinDistance.trySet(c.spawnMinDistance.get());
            fzzyConfig.general.spawnMaxDistance.trySet(c.spawnMaxDistance.get());
            fzzyConfig.general.debugLogging.trySet(c.debugLogging.get());

            fzzyConfig.friend.friendEnabled.trySet(c.friendEnabled.get());
            fzzyConfig.friend.friendEveningTime.trySet(c.friendEveningTime.get());
            fzzyConfig.friend.friendDisplayName.trySet(c.friendDisplayName.get());
            fzzyConfig.friend.friendJoinBroadcast.trySet(c.friendJoinBroadcast.get());
            fzzyConfig.friend.friendAmbientChatEnabled.trySet(c.friendAmbientChatEnabled.get());
            fzzyConfig.friend.friendAmbientChatIntervalTicks.trySet(c.friendAmbientChatIntervalTicks.get());
            fzzyConfig.friend.friendChatResponsesEnabled.trySet(c.friendChatResponsesEnabled.get());
            fzzyConfig.friend.friendProfanityPunishmentEnabled.trySet(c.friendProfanityPunishmentEnabled.get());
            fzzyConfig.friend.friendStalkerEnabled.trySet(c.friendStalkerEnabled.get());
            fzzyConfig.friend.friendFunWorldWarpChance.trySet(c.friendFunWorldWarpChance.get());
            fzzyConfig.friend.friendDoorInteractionEnabled.trySet(c.friendDoorInteractionEnabled.get());

            fzzyConfig.hands.handsEnabled.trySet(c.handsEnabled.get());
            fzzyConfig.hands.handsCaveSpawnChance.trySet(c.handsCaveSpawnChance.get());
            fzzyConfig.hands.handsChaseDurationTicks.trySet(c.handsChaseDurationTicks.get());
            fzzyConfig.hands.handsBreakBlocks.trySet(c.handsBreakBlocks.get());
            fzzyConfig.hands.handsKickOnKill.trySet(c.handsKickOnKill.get());

            fzzyConfig.treeMimic.treeMimicEnabled.trySet(c.treeMimicEnabled.get());
            fzzyConfig.treeMimic.treeMimicSpawnChance.trySet(c.treeMimicSpawnChance.get());
            fzzyConfig.treeMimic.treeMimicTriggerRange.trySet(c.treeMimicTriggerRange.get());
            fzzyConfig.treeMimic.treeMimicRequireAxe.trySet(c.treeMimicRequireAxe.get());

            fzzyConfig.theSky.theSkyEnabled.trySet(c.theSkyEnabled.get());
            fzzyConfig.theSky.theSkySpawnChance.trySet(c.theSkySpawnChance.get());
            fzzyConfig.theSky.theSkyVanishRange.trySet(c.theSkyVanishRange.get());
            fzzyConfig.theSky.theSkyStompChance.trySet(c.theSkyStompChance.get());

            fzzyConfig.dimensions.funWorldEnabled.trySet(c.funWorldEnabled.get());
            fzzyConfig.dimensions.eyesOfFunEnabled.trySet(c.eyesOfFunEnabled.get());
            fzzyConfig.dimensions.eyesOfFunInstantKill.trySet(c.eyesOfFunInstantKill.get());
            fzzyConfig.dimensions.mazeEnabled.trySet(c.mazeEnabled.get());
            fzzyConfig.dimensions.smileSprintSpeed.trySet(c.smileSprintSpeed.get());

            fzzyConfig.save();
        } finally {
            SYNCING.set(false);
        }
    }

    public static boolean isModEnabled() {
        return fzzyConfig == null || fzzyConfig.general.modEnabled.get();
    }

    public static int getSpawnMinDistance() {
        if (fzzyConfig == null) return 15;
        int min = fzzyConfig.general.spawnMinDistance.get();
        int max = fzzyConfig.general.spawnMaxDistance.get();
        return Math.min(min, max);
    }

    public static int getSpawnMaxDistance() {
        if (fzzyConfig == null) return 20;
        int min = fzzyConfig.general.spawnMinDistance.get();
        int max = fzzyConfig.general.spawnMaxDistance.get();
        return Math.max(min, max);
    }

    public static String getFriendDisplayName() {
        if (fzzyConfig == null) return "friend";
        String name = fzzyConfig.friend.friendDisplayName.get().trim();
        return name.isEmpty() ? "friend" : name;
    }

    public static boolean isScreenAtmosphereEnabled() {
        return !UnderWorldForgeConfig.CLIENT_SPEC.isLoaded() || UnderWorldForgeConfig.CLIENT.screenAtmosphereEffects.get();
    }
}
