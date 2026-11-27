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
            c.antiSpawnKillEnabled.set(fzzyConfig.general.antiSpawnKillEnabled.get());
            c.dynamicStructureEventsEnabled.set(fzzyConfig.general.dynamicStructureEventsEnabled.get());
            c.dynamicStructureIntervalTicks.set(fzzyConfig.general.dynamicStructureIntervalTicks.get());

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

            c.theTruthEnabled.set(fzzyConfig.theTruth.theTruthEnabled.get());
            c.theTruthSpawnChance.set(fzzyConfig.theTruth.theTruthSpawnChance.get());
            c.theTruthChaseDurationTicks.set(fzzyConfig.theTruth.theTruthChaseDurationTicks.get());
            c.theTruthChaseSpeed.set(fzzyConfig.theTruth.theTruthChaseSpeed.get());
            c.theTruthBreakBlocks.set(fzzyConfig.theTruth.theTruthBreakBlocks.get());
            c.theTruthKickOnKill.set(fzzyConfig.theTruth.theTruthKickOnKill.get());
            c.theTruthSpawnPillarOnRejoin.set(fzzyConfig.theTruth.theTruthSpawnPillarOnRejoin.get());

            c.clockEnabled.set(fzzyConfig.clock.clockEnabled.get());
            c.clockSpawnChance.set(fzzyConfig.clock.clockSpawnChance.get());
            c.clockTimeSpeed.set(fzzyConfig.clock.clockTimeSpeed.get());

            c.theMimicEnabled.set(fzzyConfig.theMimic.theMimicEnabled.get());
            c.theMimicSpawnChance.set(fzzyConfig.theMimic.theMimicSpawnChance.get());
            c.theMimicTriggerRange.set(fzzyConfig.theMimic.theMimicTriggerRange.get());
            c.theMimicChaseDurationTicks.set(fzzyConfig.theMimic.theMimicChaseDurationTicks.get());

            c.demonFishEnabled.set(fzzyConfig.demonFish.demonFishEnabled.get());
            c.demonFishSpawnChance.set(fzzyConfig.demonFish.demonFishSpawnChance.get());
            c.demonFishChaseDurationTicks.set(fzzyConfig.demonFish.demonFishChaseDurationTicks.get());
            c.demonFishBreakBlocks.set(fzzyConfig.demonFish.demonFishBreakBlocks.get());

            c.theBallEnabled.set(fzzyConfig.theBall.theBallEnabled.get());
            c.theBallSpawnChance.set(fzzyConfig.theBall.theBallSpawnChance.get());
            c.theBallEffectDurationTicks.set(fzzyConfig.theBall.theBallEffectDurationTicks.get());

            c.funWorldEnabled.set(fzzyConfig.dimensions.funWorldEnabled.get());
            c.funWorldTimeSpeedMultiplier.set(fzzyConfig.dimensions.funWorldTimeSpeedMultiplier.get());
            c.eyesOfFunEnabled.set(fzzyConfig.dimensions.eyesOfFunEnabled.get());
            c.eyesOfFunInstantKill.set(fzzyConfig.dimensions.eyesOfFunInstantKill.get());
            c.mazeEnabled.set(fzzyConfig.dimensions.mazeEnabled.get());
            c.smileSprintSpeed.set(fzzyConfig.dimensions.smileSprintSpeed.get());
            c.errorDimensionEnabled.set(fzzyConfig.dimensions.errorDimensionEnabled.get());
            c.errorBedWarpChance.set(fzzyConfig.dimensions.errorBedWarpChance.get());
            c.errorStayDurationTicks.set(fzzyConfig.dimensions.errorStayDurationTicks.get());
            c.timeDimensionEnabled.set(fzzyConfig.dimensions.timeDimensionEnabled.get());
            c.clockTimeHuntEnabled.set(fzzyConfig.dimensions.clockTimeHuntEnabled.get());
            c.customMusicEnabled.set(fzzyConfig.dimensions.customMusicEnabled.get());
            c.customOverworldMusic.set(fzzyConfig.dimensions.customOverworldMusic.get());

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
            fzzyConfig.general.antiSpawnKillEnabled.trySet(c.antiSpawnKillEnabled.get());
            fzzyConfig.general.dynamicStructureEventsEnabled.trySet(c.dynamicStructureEventsEnabled.get());
            fzzyConfig.general.dynamicStructureIntervalTicks.trySet(c.dynamicStructureIntervalTicks.get());

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

            fzzyConfig.theTruth.theTruthEnabled.trySet(c.theTruthEnabled.get());
            fzzyConfig.theTruth.theTruthSpawnChance.trySet(c.theTruthSpawnChance.get());
            fzzyConfig.theTruth.theTruthChaseDurationTicks.trySet(c.theTruthChaseDurationTicks.get());
            fzzyConfig.theTruth.theTruthChaseSpeed.trySet(c.theTruthChaseSpeed.get());
            fzzyConfig.theTruth.theTruthBreakBlocks.trySet(c.theTruthBreakBlocks.get());
            fzzyConfig.theTruth.theTruthKickOnKill.trySet(c.theTruthKickOnKill.get());
            fzzyConfig.theTruth.theTruthSpawnPillarOnRejoin.trySet(c.theTruthSpawnPillarOnRejoin.get());

            fzzyConfig.clock.clockEnabled.trySet(c.clockEnabled.get());
            fzzyConfig.clock.clockSpawnChance.trySet(c.clockSpawnChance.get());
            fzzyConfig.clock.clockTimeSpeed.trySet(c.clockTimeSpeed.get());

            fzzyConfig.theMimic.theMimicEnabled.trySet(c.theMimicEnabled.get());
            fzzyConfig.theMimic.theMimicSpawnChance.trySet(c.theMimicSpawnChance.get());
            fzzyConfig.theMimic.theMimicTriggerRange.trySet(c.theMimicTriggerRange.get());
            fzzyConfig.theMimic.theMimicChaseDurationTicks.trySet(c.theMimicChaseDurationTicks.get());

            fzzyConfig.demonFish.demonFishEnabled.trySet(c.demonFishEnabled.get());
            fzzyConfig.demonFish.demonFishSpawnChance.trySet(c.demonFishSpawnChance.get());
            fzzyConfig.demonFish.demonFishChaseDurationTicks.trySet(c.demonFishChaseDurationTicks.get());
            fzzyConfig.demonFish.demonFishBreakBlocks.trySet(c.demonFishBreakBlocks.get());

            fzzyConfig.theBall.theBallEnabled.trySet(c.theBallEnabled.get());
            fzzyConfig.theBall.theBallSpawnChance.trySet(c.theBallSpawnChance.get());
            fzzyConfig.theBall.theBallEffectDurationTicks.trySet(c.theBallEffectDurationTicks.get());

            fzzyConfig.dimensions.funWorldEnabled.trySet(c.funWorldEnabled.get());
            fzzyConfig.dimensions.funWorldTimeSpeedMultiplier.trySet(c.funWorldTimeSpeedMultiplier.get());
            fzzyConfig.dimensions.eyesOfFunEnabled.trySet(c.eyesOfFunEnabled.get());
            fzzyConfig.dimensions.eyesOfFunInstantKill.trySet(c.eyesOfFunInstantKill.get());
            fzzyConfig.dimensions.mazeEnabled.trySet(c.mazeEnabled.get());
            fzzyConfig.dimensions.smileSprintSpeed.trySet(c.smileSprintSpeed.get());
            fzzyConfig.dimensions.errorDimensionEnabled.trySet(c.errorDimensionEnabled.get());
            fzzyConfig.dimensions.errorBedWarpChance.trySet(c.errorBedWarpChance.get());
            fzzyConfig.dimensions.errorStayDurationTicks.trySet(c.errorStayDurationTicks.get());
            fzzyConfig.dimensions.timeDimensionEnabled.trySet(c.timeDimensionEnabled.get());
            fzzyConfig.dimensions.clockTimeHuntEnabled.trySet(c.clockTimeHuntEnabled.get());
            fzzyConfig.dimensions.customMusicEnabled.trySet(c.customMusicEnabled.get());
            fzzyConfig.dimensions.customOverworldMusic.trySet(c.customOverworldMusic.get());

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

    public static double getErrorBedWarpChance() {
        return fzzyConfig == null ? 0.20D : fzzyConfig.dimensions.errorBedWarpChance.get();
    }

    public static int getErrorStayDurationTicks() {
        return fzzyConfig == null ? 1800 : fzzyConfig.dimensions.errorStayDurationTicks.get();
    }

    public static double getClockTimeSpeed() {
        return fzzyConfig == null ? 0.42D : fzzyConfig.clock.clockTimeSpeed.get();
    }

    public static boolean isCustomMusicEnabled() {
        return fzzyConfig == null || fzzyConfig.dimensions.customMusicEnabled.get();
    }

    public static boolean isOverworldCustomMusicEnabled() {
        return fzzyConfig == null || fzzyConfig.dimensions.customOverworldMusic.get();
    }

    public static boolean isDynamicStructureEventsEnabled() {
        return fzzyConfig == null || fzzyConfig.general.dynamicStructureEventsEnabled.get();
    }

    public static int getDynamicStructureIntervalTicks() {
        return fzzyConfig == null ? 4800 : fzzyConfig.general.dynamicStructureIntervalTicks.get();
    }

    public static double getTheMimicTriggerRange() {
        return fzzyConfig == null ? 9.5D : fzzyConfig.theMimic.theMimicTriggerRange.get();
    }

    public static int getTheMimicChaseDurationTicks() {
        return fzzyConfig == null ? 600 : fzzyConfig.theMimic.theMimicChaseDurationTicks.get();
    }

    public static int getDemonFishChaseDurationTicks() {
        return fzzyConfig == null ? 1200 : fzzyConfig.demonFish.demonFishChaseDurationTicks.get();
    }

    public static boolean isDemonFishBreakBlocksEnabled() {
        return fzzyConfig == null || fzzyConfig.demonFish.demonFishBreakBlocks.get();
    }

    public static int getTheBallEffectDurationTicks() {
        return fzzyConfig == null ? 400 : fzzyConfig.theBall.theBallEffectDurationTicks.get();
    }
}
