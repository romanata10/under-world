package com.underworld.mod.event;

import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.config.UnderWorldConfigHolder;
import com.underworld.mod.entity.ClockEntity;
import com.underworld.mod.entity.DemonFishEntity;
import com.underworld.mod.entity.EyesOfFunEntity;
import com.underworld.mod.entity.FriendEntity;
import com.underworld.mod.entity.HandsEntity;
import com.underworld.mod.entity.LivingPylonEntity;
import com.underworld.mod.entity.SmileEntity;
import com.underworld.mod.entity.TheBallEntity;
import com.underworld.mod.entity.TheDotComEntity;
import com.underworld.mod.entity.TheMimicEntity;
import com.underworld.mod.entity.TheSkyEntity;
import com.underworld.mod.entity.TheTruthEntity;
import com.underworld.mod.entity.TreeMimicEntity;
import com.underworld.mod.entity.WindTurbineStalkerEntity;
import com.underworld.mod.network.NetworkHandler;
import com.underworld.mod.registry.ModBlocks;
import com.underworld.mod.registry.ModDimensions;
import com.underworld.mod.registry.ModEntities;
import com.underworld.mod.registry.ModSounds;
import com.underworld.mod.world.FunWorldTimeManager;
import com.underworld.mod.world.UnderWorldSavedData;
import com.underworld.mod.world.dimension.BrowserChunkGenerator;
import com.underworld.mod.world.dimension.ErrorChunkGenerator;
import com.underworld.mod.world.dimension.MazeChunkGenerator;
import com.underworld.mod.world.dimension.TimeChunkGenerator;
import com.underworld.mod.world.dimension.WindOceanChunkGenerator;
import com.underworld.mod.world.structure.VoidShrinePiece;
import com.underworld.mod.world.structure.lore.LoreStructureSpawner;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class UnderWorldEventDirector {
    private static final String[] AMBIENT_FRIEND_MESSAGES = {
            "hello?",
            "im your friend",
            "the void",
            "look behind you",
            "come play in the flowers",
            "red bricks.."
    };

    private static final List<ScheduledAction> SCHEDULED_ACTIONS = new ArrayList<>();
    private static final List<PendingKick> PENDING_KICKS = new ArrayList<>();
    private static final Map<UUID, Long> SPAWN_PROTECTION_UNTIL_TICK = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> BED_WARP_ROLLS = new ConcurrentHashMap<>();

    private UnderWorldEventDirector() {}

    private static final class ScheduledAction {
        int ticksRemaining;
        final Runnable action;

        ScheduledAction(int ticksRemaining, Runnable action) {
            this.ticksRemaining = ticksRemaining;
            this.action = action;
        }
    }

    private static final class PendingKick {
        final UUID playerUuid;
        int ticksRemaining;
        final Component reason;

        PendingKick(UUID playerUuid, int ticksRemaining, Component reason) {
            this.playerUuid = playerUuid;
            this.ticksRemaining = ticksRemaining;
            this.reason = reason;
        }
    }

    public static void schedule(int delayTicks, Runnable action) {
        synchronized (SCHEDULED_ACTIONS) {
            SCHEDULED_ACTIONS.add(new ScheduledAction(Math.max(1, delayTicks), action));
        }
    }

    public static void schedulePlayerKick(ServerPlayer player, int delayTicks, Component reason) {
        synchronized (PENDING_KICKS) {
            PENDING_KICKS.add(new PendingKick(player.getUUID(), Math.max(1, delayTicks), reason));
        }
    }

    @SubscribeEvent
    public static void onServerStarting(final ServerStartingEvent event) {
        FunWorldTimeManager.bindServer(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(final ServerStoppedEvent event) {
        FunWorldTimeManager.unbindServer();
        synchronized (SCHEDULED_ACTIONS) {
            SCHEDULED_ACTIONS.clear();
        }
        synchronized (PENDING_KICKS) {
            PENDING_KICKS.clear();
        }
        SPAWN_PROTECTION_UNTIL_TICK.clear();
        BED_WARP_ROLLS.clear();
    }

    @SubscribeEvent
    public static void onLivingHurt(final LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (ModDimensions.isError(player.level())) {
                // The Error is a safe glitch anomaly dimension
                event.setCanceled(true);
                return;
            }
            if (ModDimensions.isTime(player.level()) && event.getSource().is(DamageTypeTags.IS_FALL)) {
                // Prevent fatal fall-damage loops on endless clock discs in The Time
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        UnderWorldSavedData data = UnderWorldSavedData.get(server);
        if (data.isFriendInTabList()) {
            NetworkHandler.syncFriendTabToPlayer(player, true, UnderWorldConfigHolder.getFriendDisplayName());
        }
        if (ModDimensions.isFunWorld(player.level())) {
            FunWorldTimeManager.syncToPlayer(player);
        }
        syncTruthHudForPlayerDimension(player);

        if (player.isAlive() && data.hasPendingTruthPillar(player.getUUID())) {
            UUID id = player.getUUID();
            schedule(2, () -> trySpawnTruthPillarForPlayer(server, id));
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(final PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (event.getTo().equals(ModDimensions.THE_FUN_WORLD)) {
            FunWorldTimeManager.syncToPlayer(player);
        }
        syncTruthHudForPlayerDimension(player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(final PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        grantSpawnProtection(player, 300);
        if (UnderWorldConfigHolder.get() == null || UnderWorldConfigHolder.get().general.antiSpawnKillEnabled.get()) {
            discardNearbyHostileUnderWorld(player.serverLevel(), player, 24.0D);
        }
        if (ModDimensions.isFunWorld(player.level())) {
            FunWorldTimeManager.syncToPlayer(player);
        }
        syncTruthHudForPlayerDimension(player);

        MinecraftServer server = player.getServer();
        if (server != null && UnderWorldSavedData.get(server).hasPendingTruthPillar(player.getUUID())) {
            UUID id = player.getUUID();
            schedule(2, () -> trySpawnTruthPillarForPlayer(server, id));
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(final LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.isCanceled()) {
            return;
        }
        if (UnderWorldConfigHolder.get() != null && !UnderWorldConfigHolder.get().general.antiSpawnKillEnabled.get()) {
            return;
        }

        ServerLevel level = player.serverLevel();
        Entity sourceEntity = event.getSource().getEntity();
        Entity directEntity = event.getSource().getDirectEntity();
        LivingEntity lastHurtMob = player.getLastHurtByMob();

        boolean killedByUnderWorld = isUnderWorldEntity(sourceEntity)
                || isUnderWorldEntity(directEntity)
                || isUnderWorldEntity(lastHurtMob);

        List<Entity> toRemove = new ArrayList<>();
        if (isUnderWorldEntity(sourceEntity)) toRemove.add(sourceEntity);
        if (isUnderWorldEntity(directEntity) && !toRemove.contains(directEntity)) toRemove.add(directEntity);
        if (isUnderWorldEntity(lastHurtMob) && !toRemove.contains(lastHurtMob)) toRemove.add(lastHurtMob);

        AABB radiusBox = AABB.ofSize(player.position(), 64.0D, 48.0D, 64.0D);
        for (Entity nearby : level.getEntities(player, radiusBox, UnderWorldEventDirector::isUnderWorldEntity)) {
            if (shouldVanishOnPlayerDeath(nearby, player, killedByUnderWorld) && !toRemove.contains(nearby)) {
                toRemove.add(nearby);
            }
        }

        if (toRemove.isEmpty() && !killedByUnderWorld) {
            return;
        }

        for (Entity entity : toRemove) {
            if (entity.isAlive()) {
                level.sendParticles(
                        ParticleTypes.LARGE_SMOKE,
                        entity.getX(),
                        entity.getY() + Math.min(2.0D, entity.getBbHeight() * 0.5D),
                        entity.getZ(),
                        14, 0.6D, 0.8D, 0.6D, 0.04D
                );
                if (entity instanceof TheTruthEntity) {
                    NetworkHandler.syncTruthHudToDimension(level, false, 0, 600);
                }
                entity.discard();
            }
        }

        grantSpawnProtection(player, 300);
        MinecraftServer server = player.getServer();
        if (server != null) {
            UnderWorldSavedData data = UnderWorldSavedData.get(server);
            data.setEventCooldownTicks(Math.max(data.getEventCooldownTicks(), 400));
        }
    }

    @SubscribeEvent
    public static void onServerTick(final TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = event.getServer();
        processScheduledActions();
        processPendingKicks(server);

        if (server.getPlayerList().getPlayerCount() == 0) {
            return;
        }

        // Advance accelerated Fun World day/night clock independently of the Overworld
        FunWorldTimeManager.tickServer(server);

        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }

        UnderWorldSavedData data = UnderWorldSavedData.get(server);

        // Void Shrine -> The Fun World ritual trigger (always active when funWorldEnabled)
        if (server.getTickCount() % 10 == 0
                && (UnderWorldConfigHolder.get() == null || UnderWorldConfigHolder.get().dimensions.funWorldEnabled.get())) {
            checkVoidShrineWarps(overworld);
        }

        // Sleep in Overworld bed -> 20% chance warp to The Error
        if (UnderWorldConfigHolder.get() == null || UnderWorldConfigHolder.get().dimensions.errorDimensionEnabled.get()) {
            tickBedErrorWarps(overworld);
        }

        // Safe timer & return for players inside The Error, plus void loop & Clock hunter inside The Time
        tickErrorDimension(server);
        tickTimeDimension(server);

        // v2.4.0 Dimension directors (The Plains night pylons, Wind Ocean stalking turbines, The Xp falling icons, Browser URL & trap)
        tickXpDimension(server);
        if (server.getTickCount() % 20 == 0) {
            tickBrowserDimension(server);
        }
        if (server.getTickCount() % 80 == 0) {
            tickFunWorld(server);
            tickMaze(server);
            tickPlainsDimension(server);
            tickWindOceanDimension(server);
        }

        if (!UnderWorldConfigHolder.isModEnabled() || data.areEventsPaused()) {
            return;
        }

        // 1. One-time per world Day 1 evening friend join event
        if (!data.hasFriendJoined() && UnderWorldConfigHolder.get().friend.friendEnabled.get()) {
            long eveningTime = UnderWorldConfigHolder.get().friend.friendEveningTime.get();
            long dayTime = overworld.getDayTime();
            if (dayTime >= eveningTime && (dayTime % 24000L) >= eveningTime) {
                triggerFriendJoinEvent(server, overworld, pickSurvivalPlayer(overworld));
            }
        }

        // 2. Ambient friend chat whispers after join
        if (data.hasFriendJoined() && UnderWorldConfigHolder.get().friend.friendAmbientChatEnabled.get()) {
            int timer = data.getAmbientChatTimerTicks() - 1;
            if (timer <= 0) {
                String msg = triggerFriendAmbientChat(server);
                if (UnderWorldConfigHolder.get().general.debugLogging.get()) {
                    UnderWorldMod.LOGGER.info("friend ambient chat sent: {}", msg);
                }
                int interval = UnderWorldConfigHolder.get().friend.friendAmbientChatIntervalTicks.get();
                data.setAmbientChatTimerTicks(interval + overworld.getRandom().nextInt(Math.max(100, interval / 2)));
            } else {
                data.setAmbientChatTimerTicks(timer);
            }
        }

        // 2b. Physical Friend Stalker spawning when absent
        if (data.hasFriendJoined() && UnderWorldConfigHolder.get().friend.friendStalkerEnabled.get()) {
            int stalkCd = data.getFriendStalkCooldownTicks() - 1;
            if (stalkCd <= 0) {
                data.setFriendStalkCooldownTicks(1800 + overworld.getRandom().nextInt(1800));
                if (!hasFriendInServer(server, data)) {
                    ServerPlayer target = pickSurvivalPlayer(overworld);
                    if (target != null) {
                        spawnFriendNear(overworld, target, data);
                    }
                }
            } else {
                data.setFriendStalkCooldownTicks(stalkCd);
            }
        }

        // 2c. Dynamic survival structure events (both classic oddity/nostalgia & server-lore builds)
        if (UnderWorldConfigHolder.isDynamicStructureEventsEnabled()) {
            int structCd = data.getStructureEventCooldownTicks() - 1;
            if (structCd <= 0) {
                int interval = UnderWorldConfigHolder.getDynamicStructureIntervalTicks();
                data.setStructureEventCooldownTicks(interval + overworld.getRandom().nextInt(Math.max(200, interval / 2)));
                ServerPlayer structTarget = pickSurvivalPlayer(overworld);
                if (structTarget != null && !isSpawnProtected(structTarget)) {
                    String spawned = LoreStructureSpawner.trySpawnDynamicStructureAround(overworld, structTarget, data);
                    if (spawned != null && UnderWorldConfigHolder.get().general.debugLogging.get()) {
                        UnderWorldMod.LOGGER.info("Dynamic survival structure {} appeared near {}", spawned, structTarget.getGameProfile().getName());
                    }
                }
            } else {
                data.setStructureEventCooldownTicks(structCd);
            }
        }

        // 3. Rare automatic encounters checked every 5 seconds (100 ticks)
        if (data.getTruthCooldownTicks() > 0) {
            data.setTruthCooldownTicks(data.getTruthCooldownTicks() - 1);
        }

        int cooldown = data.getEventCooldownTicks();
        if (cooldown > 0) {
            data.setEventCooldownTicks(cooldown - 1);
            return;
        }

        if (server.getTickCount() % 100 != 0) {
            return;
        }

        ServerPlayer target = pickSurvivalPlayer(overworld);
        if (target == null || isSpawnProtected(target)) {
            return;
        }

        if (hasNearbyHorrorEntity(overworld, target.position(), 64.0D)) {
            return;
        }

        RandomSource random = overworld.getRandom();
        BlockPos playerPos = target.blockPosition();
        boolean seeSky = overworld.canSeeSky(playerPos);

        // Aquatic Demon Fish hunt when swimming or boating in water!
        boolean inWaterOrBoat = target.isInWaterOrBubble()
                || target.getVehicle() instanceof Boat
                || overworld.getFluidState(playerPos.below()).is(FluidTags.WATER);
        if (inWaterOrBoat && UnderWorldConfigHolder.get().demonFish.demonFishEnabled.get()) {
            double dfChance = UnderWorldConfigHolder.get().demonFish.demonFishSpawnChance.get() * 0.30D;
            if (random.nextDouble() < dfChance) {
                if (triggerDemonFishEncounter(overworld, target) != null) {
                    data.setEventCooldownTicks(3200 + random.nextInt(2400));
                    return;
                }
            }
        }

        // Super-mega-rare automatic event: The Truth
        if (data.getTruthCooldownTicks() <= 0
                && !hasActiveTruth(server)
                && UnderWorldConfigHolder.get().theTruth.theTruthEnabled.get()) {
            double truthChance = UnderWorldConfigHolder.get().theTruth.theTruthSpawnChance.get() * 0.12D;
            if (random.nextDouble() < truthChance) {
                if (triggerTheTruthEncounter(overworld, target) != null) {
                    data.setTruthCooldownTicks(36000 + random.nextInt(36000));
                    data.setEventCooldownTicks(6000 + random.nextInt(6000));
                    return;
                }
            }
        }

        // Cave encounter: Hands
        if (!seeSky && target.getY() < 48.0D
                && overworld.getMaxLocalRawBrightness(playerPos) <= 6
                && UnderWorldConfigHolder.get().hands.handsEnabled.get()) {
            double chance = UnderWorldConfigHolder.get().hands.handsCaveSpawnChance.get() * 0.20D;
            if (random.nextDouble() < chance) {
                if (triggerHandsEncounter(overworld, target) != null) {
                    data.setEventCooldownTicks(3600 + random.nextInt(3600));
                    return;
                }
            }
        }

        // Surface encounters: The Sky, Tree?, Clock, The Mimic, The Ball
        if (seeSky) {
            if (UnderWorldConfigHolder.get().theSky.theSkyEnabled.get()) {
                double skyChance = UnderWorldConfigHolder.get().theSky.theSkySpawnChance.get() * 0.15D;
                if (random.nextDouble() < skyChance) {
                    if (triggerTheSkyEncounter(overworld, target, 50, 80) != null) {
                        data.setEventCooldownTicks(4800 + random.nextInt(4800));
                        return;
                    }
                }
            }

            if (UnderWorldConfigHolder.get().theMimic.theMimicEnabled.get()) {
                double mimicChance = UnderWorldConfigHolder.get().theMimic.theMimicSpawnChance.get() * 0.20D;
                if (random.nextDouble() < mimicChance) {
                    if (triggerTheMimicEncounter(overworld, target) != null) {
                        data.setEventCooldownTicks(3400 + random.nextInt(3000));
                        return;
                    }
                }
            }

            if (UnderWorldConfigHolder.get().theBall.theBallEnabled.get()) {
                double ballChance = UnderWorldConfigHolder.get().theBall.theBallSpawnChance.get() * 0.18D;
                if (random.nextDouble() < ballChance) {
                    if (triggerTheBallEncounter(overworld, target) != null) {
                        data.setEventCooldownTicks(3000 + random.nextInt(3000));
                        return;
                    }
                }
            }

            if (UnderWorldConfigHolder.get().treeMimic.treeMimicEnabled.get()) {
                Holder<Biome> biome = overworld.getBiome(playerPos);
                boolean wooded = biome.is(BiomeTags.IS_FOREST) || biome.is(BiomeTags.IS_TAIGA);
                double treeChance = UnderWorldConfigHolder.get().treeMimic.treeMimicSpawnChance.get() * (wooded ? 0.22D : 0.08D);
                if (random.nextDouble() < treeChance) {
                    if (triggerTreeEncounter(overworld, target) != null) {
                        data.setEventCooldownTicks(3600 + random.nextInt(3600));
                        return;
                    }
                }
            }

            if (UnderWorldConfigHolder.get().clock.clockEnabled.get()
                    && UnderWorldConfigHolder.get().dimensions.timeDimensionEnabled.get()) {
                double clockChance = UnderWorldConfigHolder.get().clock.clockSpawnChance.get() * 0.16D;
                if (random.nextDouble() < clockChance) {
                    if (triggerClockEncounter(overworld, target, false) != null) {
                        data.setEventCooldownTicks(4200 + random.nextInt(3600));
                        return;
                    }
                }
            }

            // Super-rare night horror event: "welcome to the hell"
            if (overworld.isNight() && random.nextDouble() < 0.008D) {
                Advancement hellAdv = server.getAdvancements().getAdvancement(UnderWorldMod.id("welcome_to_the_hell"));
                if (hellAdv != null && !target.getAdvancements().getOrStartProgress(hellAdv).isDone()) {
                    triggerWelcomeToTheHellEvent(server, target);
                    data.setEventCooldownTicks(6000 + random.nextInt(4800));
                }
            }
        }
    }

    private static void tickBedErrorWarps(ServerLevel overworld) {
        double chance = UnderWorldConfigHolder.getErrorBedWarpChance();
        for (ServerPlayer player : List.copyOf(overworld.players())) {
            UUID id = player.getUUID();
            if (!player.isSleeping()) {
                BED_WARP_ROLLS.remove(id);
                continue;
            }
            Boolean willWarp = BED_WARP_ROLLS.get(id);
            if (willWarp == null) {
                willWarp = overworld.getRandom().nextDouble() < chance;
                BED_WARP_ROLLS.put(id, willWarp);
            }
            if (willWarp && player.getSleepTimer() >= 35) {
                BED_WARP_ROLLS.remove(id);
                player.stopSleepInBed(true, true);
                ModDimensions.sendToError(player);
            }
        }
    }

    private static void tickErrorDimension(MinecraftServer server) {
        ServerLevel errorLevel = server.getLevel(ModDimensions.THE_ERROR);
        if (errorLevel == null || errorLevel.players().isEmpty()) {
            return;
        }
        UnderWorldSavedData data = UnderWorldSavedData.get(server);
        for (ServerPlayer player : List.copyOf(errorLevel.players())) {
            player.fallDistance = 0.0F;
            if (player.getY() < -4.0D) {
                int sy = ErrorChunkGenerator.getSurfaceYAt(8, 8);
                player.teleportTo(errorLevel, 8.5D, sy + 1.0D, 8.5D, player.getYRot(), player.getXRot());
            }
            int remaining = data.getErrorReturnTimer(player.getUUID());
            if (remaining <= 0) {
                remaining = UnderWorldConfigHolder.getErrorStayDurationTicks();
                data.setErrorReturnTimer(player.getUUID(), remaining);
            }
            remaining--;
            if (remaining <= 0) {
                ModDimensions.returnFromError(player);
            } else {
                data.setErrorReturnTimer(player.getUUID(), remaining);
                if (server.getTickCount() % 20 == 0) {
                    int secTotal = (remaining + 19) / 20;
                    int mins = secTotal / 60;
                    int secs = secTotal % 60;
                    String formatted = String.format("%02d:%02d", mins, secs);
                    player.displayClientMessage(Component.translatable("message.under_world.error_timer", formatted), true);
                }
            }
        }
    }

    private static void tickTimeDimension(MinecraftServer server) {
        ServerLevel timeLevel = server.getLevel(ModDimensions.THE_TIME);
        if (timeLevel == null || timeLevel.players().isEmpty()) {
            return;
        }
        for (ServerPlayer player : List.copyOf(timeLevel.players())) {
            // Endless sky-clock void wrap if falling off a clock disc — safe drop height + extended slow falling
            if (player.getY() < -12.0D) {
                BlockPos disc = TimeChunkGenerator.findNearestSafeDiscPos(player.getBlockX(), player.getBlockZ());
                player.fallDistance = 0.0F;
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 300, 0, false, false));
                player.teleportTo(timeLevel, disc.getX() + 0.5D, disc.getY() + 18.0D, disc.getZ() + 0.5D, player.getYRot(), player.getXRot());
            }

            if (server.getTickCount() % 80 != 0) {
                continue;
            }
            if (UnderWorldConfigHolder.get() != null && !UnderWorldConfigHolder.get().dimensions.clockTimeHuntEnabled.get()) {
                continue;
            }
            if (player.isCreative() || player.isSpectator() || !player.isAlive() || isSpawnProtected(player)) {
                continue;
            }
            AABB nearby = AABB.ofSize(player.position(), 88.0D, 64.0D, 88.0D);
            int existing = timeLevel.getEntitiesOfClass(ClockEntity.class, nearby).size();
            if (existing == 0 && timeLevel.getRandom().nextFloat() < 0.70F) {
                triggerClockEncounter(timeLevel, player, true);
            }
        }
    }

    private static void checkVoidShrineWarps(ServerLevel overworld) {
        for (ServerPlayer player : List.copyOf(overworld.players())) {
            if (player.isSpectator() || player.isOnPortalCooldown()) {
                continue;
            }
            if (VoidShrinePiece.isPlayerInsideVoidShrine(overworld, player.blockPosition())) {
                player.setPortalCooldown(100);
                ModDimensions.sendToFunWorld(player);
            }
        }
    }

    private static void tickFunWorld(MinecraftServer server) {
        if (UnderWorldConfigHolder.get() != null && !UnderWorldConfigHolder.get().dimensions.eyesOfFunEnabled.get()) {
            return;
        }
        ServerLevel funLevel = server.getLevel(ModDimensions.THE_FUN_WORLD);
        if (funLevel == null || funLevel.players().isEmpty()) {
            return;
        }
        long timeOfDay = funLevel.getDayTime() % 24000L;
        if (timeOfDay < 12600L || timeOfDay > 23400L) {
            return;
        }

        for (ServerPlayer player : List.copyOf(funLevel.players())) {
            if (player.isCreative() || player.isSpectator() || !player.isAlive() || isSpawnProtected(player)) {
                continue;
            }
            AABB nearby = AABB.ofSize(player.position(), 72.0D, 48.0D, 72.0D);
            int existing = funLevel.getEntitiesOfClass(EyesOfFunEntity.class, nearby).size();
            if (existing < 2 && funLevel.getRandom().nextFloat() < 0.45F) {
                triggerEyesOfFunEncounter(funLevel, player);
            }
        }
    }

    private static void tickMaze(MinecraftServer server) {
        if (UnderWorldConfigHolder.get() != null && !UnderWorldConfigHolder.get().dimensions.mazeEnabled.get()) {
            return;
        }
        ServerLevel mazeLevel = server.getLevel(ModDimensions.MAZE);
        if (mazeLevel == null || mazeLevel.players().isEmpty()) {
            return;
        }

        for (ServerPlayer player : List.copyOf(mazeLevel.players())) {
            if (player.isCreative() || player.isSpectator() || !player.isAlive() || isSpawnProtected(player)) {
                continue;
            }
            AABB nearby = AABB.ofSize(player.position(), 96.0D, 32.0D, 96.0D);
            int existing = mazeLevel.getEntitiesOfClass(SmileEntity.class, nearby).size();
            if (existing == 0 && mazeLevel.getRandom().nextFloat() < 0.55F) {
                triggerSmileEncounter(mazeLevel, player);
            }
        }
    }

    private static void tickPlainsDimension(MinecraftServer server) {
        ServerLevel plains = server.getLevel(ModDimensions.THE_PLAINS);
        if (plains == null || plains.players().isEmpty() || !plains.isNight()) {
            return;
        }
        for (ServerPlayer player : List.copyOf(plains.players())) {
            if (player.isCreative() || player.isSpectator() || !player.isAlive() || isSpawnProtected(player)) {
                continue;
            }
            AABB nearby = AABB.ofSize(player.position(), 88.0D, 48.0D, 88.0D);
            int existing = plains.getEntitiesOfClass(LivingPylonEntity.class, nearby).size();
            if (existing == 0 && plains.getRandom().nextFloat() < 0.45F) {
                triggerLivingPylonEncounter(plains, player);
            }
        }
    }

    private static void tickWindOceanDimension(MinecraftServer server) {
        ServerLevel ocean = server.getLevel(ModDimensions.THE_WIND_OCEAN);
        if (ocean == null || ocean.players().isEmpty()) {
            return;
        }
        for (ServerPlayer player : List.copyOf(ocean.players())) {
            if (player.isCreative() || player.isSpectator() || !player.isAlive() || isSpawnProtected(player)) {
                continue;
            }
            AABB nearby = AABB.ofSize(player.position(), 96.0D, 48.0D, 96.0D);
            int existing = ocean.getEntitiesOfClass(WindTurbineStalkerEntity.class, nearby).size();
            if (existing < 2 && ocean.getRandom().nextFloat() < 0.50F) {
                triggerWindTurbineEncounter(ocean, player, false);
            }
        }
    }

    private static void tickXpDimension(MinecraftServer server) {
        if (server.getTickCount() % 5 != 0) {
            return;
        }
        ServerLevel xpLevel = server.getLevel(ModDimensions.THE_XP);
        if (xpLevel == null || xpLevel.players().isEmpty()) {
            return;
        }
        for (ServerPlayer player : List.copyOf(xpLevel.players())) {
            BlockPos pPos = player.blockPosition();
            boolean corruptedAny = false;
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    for (int dy = 0; dy <= 7; dy++) {
                        BlockPos check = pPos.offset(dx, dy, dz);
                        if (xpLevel.getBlockState(check).is(ModBlocks.XP_ICON_BLOCK.get())) {
                            corruptedAny = true;
                            xpLevel.setBlock(check, Blocks.AIR.defaultBlockState(), 3);
                            FallingBlockEntity falling = FallingBlockEntity.fall(
                                    xpLevel, check, ModBlocks.XP_ICON_FALLEN.get().defaultBlockState());
                            falling.dropItem = false;
                        }
                    }
                }
            }
            if (corruptedAny) {
                xpLevel.playSound(null, pPos, ModSounds.XP_ERROR.get(), SoundSource.BLOCKS, 1.4F, 0.85F);
            }
        }
    }

    private static void tickBrowserDimension(MinecraftServer server) {
        ServerLevel browser = server.getLevel(ModDimensions.THE_BROWSER);
        if (browser == null || browser.players().isEmpty()) {
            return;
        }
        for (ServerPlayer player : List.copyOf(browser.players())) {
            String url = BrowserChunkGenerator.getSiteUrl(player.getBlockX(), player.getBlockZ());
            player.displayClientMessage(Component.literal("[Chrome] " + url).withStyle(
                    "http://the-MinECRaT-fREe-downladn.com/".equals(url) ? ChatFormatting.RED : ChatFormatting.AQUA), true);
            // Auto-trigger download trap if standing very close to DOWNLOAD button
            BlockPos pp = player.blockPosition();
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        BlockPos scan = pp.offset(dx, dy, dz);
                        if (browser.getBlockState(scan).is(ModBlocks.DOWNLOAD_BUTTON_BLOCK.get())) {
                            BrowserChunkGenerator.activateDownloadTrap(browser, scan, player);
                            triggerDotComTrap(browser, scan, player);
                            return;
                        }
                    }
                }
            }
        }
    }

    private static void processScheduledActions() {
        List<Runnable> ready = new ArrayList<>();
        synchronized (SCHEDULED_ACTIONS) {
            Iterator<ScheduledAction> it = SCHEDULED_ACTIONS.iterator();
            while (it.hasNext()) {
                ScheduledAction action = it.next();
                action.ticksRemaining--;
                if (action.ticksRemaining <= 0) {
                    ready.add(action.action);
                    it.remove();
                }
            }
        }
        for (Runnable r : ready) {
            r.run();
        }
    }

    private static void processPendingKicks(MinecraftServer server) {
        List<PendingKick> due = new ArrayList<>();
        synchronized (PENDING_KICKS) {
            Iterator<PendingKick> it = PENDING_KICKS.iterator();
            while (it.hasNext()) {
                PendingKick kick = it.next();
                kick.ticksRemaining--;
                if (kick.ticksRemaining <= 0) {
                    due.add(kick);
                    it.remove();
                }
            }
        }
        for (PendingKick kick : due) {
            ServerPlayer player = server.getPlayerList().getPlayer(kick.playerUuid);
            if (player != null) {
                player.connection.disconnect(kick.reason);
            }
        }
    }

    public static boolean triggerFriendJoinEvent(MinecraftServer server, ServerLevel level, @Nullable ServerPlayer nearPlayer) {
        UnderWorldSavedData data = UnderWorldSavedData.get(server);
        data.setFriendHasJoined(true);
        data.setFriendInTabList(true);

        String friendName = UnderWorldConfigHolder.getFriendDisplayName();
        NetworkHandler.syncFriendTabToAll(true, friendName);

        if (UnderWorldConfigHolder.get().friend.friendJoinBroadcast.get()) {
            Component joinMessage = Component.translatable("multiplayer.player.joined", friendName)
                    .withStyle(ChatFormatting.YELLOW);
            server.getPlayerList().broadcastSystemMessage(joinMessage, false);

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                player.playNotifySound(SoundEvents.AMBIENT_CAVE.get(), SoundSource.AMBIENT, 1.3F, 0.8F);
            }
        }

        if (!hasFriendInServer(server, data)) {
            ServerPlayer anchor = nearPlayer != null ? nearPlayer : pickAnyPlayer(level);
            if (anchor != null) {
                spawnFriendNear(level, anchor, data);
            }
        }

        return true;
    }

    private static void spawnFriendNear(ServerLevel level, ServerPlayer anchor, UnderWorldSavedData data) {
        BlockPos pos = findSpawnPosAroundPlayer(level, anchor,
                UnderWorldConfigHolder.getSpawnMinDistance(),
                UnderWorldConfigHolder.getSpawnMaxDistance(),
                true);
        if (pos == null) {
            pos = anchor.blockPosition().offset(18, 0, 0);
        }
        FriendEntity friend = ModEntities.FRIEND.get().create(level);
        if (friend != null) {
            friend.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                    level.getRandom().nextFloat() * 360.0F, 0.0F);
            friend.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
            level.addFreshEntity(friend);
            data.setFriendEntityUuid(friend.getUUID());
        }
    }

    public static String triggerFriendAmbientChat(MinecraftServer server) {
        String[] pool = AMBIENT_FRIEND_MESSAGES;
        String text = pool[server.overworld().getRandom().nextInt(pool.length)];
        broadcastFriendChat(server, text);
        return text;
    }

    public static void broadcastFriendChat(MinecraftServer server, String text) {
        String friendName = UnderWorldConfigHolder.getFriendDisplayName();
        Component msg = Component.literal("<" + friendName + "> " + text);
        server.getPlayerList().broadcastSystemMessage(msg, false);
    }

    @Nullable
    public static HandsEntity triggerHandsEncounter(ServerLevel level, ServerPlayer player) {
        BlockPos pos = findSpawnPosAroundPlayer(level, player,
                UnderWorldConfigHolder.getSpawnMinDistance(),
                UnderWorldConfigHolder.getSpawnMaxDistance(),
                true);
        if (pos == null) {
            return null;
        }
        HandsEntity hands = ModEntities.HANDS.get().create(level);
        if (hands == null) {
            return null;
        }
        hands.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                level.getRandom().nextFloat() * 360.0F, 0.0F);
        hands.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
        hands.setTarget(player);
        level.addFreshEntity(hands);
        return hands;
    }

    @Nullable
    public static TreeMimicEntity triggerTreeEncounter(ServerLevel level, ServerPlayer player) {
        BlockPos pos = findSpawnPosAroundPlayer(level, player,
                UnderWorldConfigHolder.getSpawnMinDistance(),
                UnderWorldConfigHolder.getSpawnMaxDistance(),
                false);
        if (pos == null) {
            return null;
        }
        TreeMimicEntity tree = ModEntities.TREE_MIMIC.get().create(level);
        if (tree == null) {
            return null;
        }
        tree.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                level.getRandom().nextFloat() * 360.0F, 0.0F);
        tree.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
        level.addFreshEntity(tree);
        level.playSound(null, pos, SoundEvents.AMBIENT_CAVE.get(), SoundSource.AMBIENT, 1.4F, 0.85F);
        return tree;
    }

    @Nullable
    public static TheSkyEntity triggerTheSkyEncounter(ServerLevel level, ServerPlayer player, int minDist, int maxDist) {
        BlockPos pos = findSpawnPosAroundPlayer(level, player, minDist, maxDist, false);
        if (pos == null) {
            return null;
        }
        TheSkyEntity sky = ModEntities.THE_SKY.get().create(level);
        if (sky == null) {
            return null;
        }
        sky.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                level.getRandom().nextFloat() * 360.0F, 0.0F);
        sky.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
        level.addFreshEntity(sky);
        return sky;
    }

    @Nullable
    public static EyesOfFunEntity triggerEyesOfFunEncounter(ServerLevel level, ServerPlayer player) {
        BlockPos pos = findSpawnPosAroundPlayer(level, player, 18, 30, false);
        if (pos == null) {
            return null;
        }
        EyesOfFunEntity eyes = ModEntities.EYES_OF_FUN.get().create(level);
        if (eyes == null) {
            return null;
        }
        eyes.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                level.getRandom().nextFloat() * 360.0F, 0.0F);
        eyes.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
        eyes.setTarget(player);
        level.addFreshEntity(eyes);
        level.playSound(null, pos, ModSounds.EYES_OF_FUN_SPOT.get(), SoundSource.HOSTILE, 0.9F, 0.95F);
        return eyes;
    }

    @Nullable
    public static SmileEntity triggerSmileEncounter(ServerLevel level, ServerPlayer player) {
        BlockPos pos = findMazeCorridorSpotAround(level, player, 20, 34);
        if (pos == null) {
            pos = findSpawnPosAroundPlayer(level, player, 16, 26, true);
        }
        if (pos == null) {
            return null;
        }
        SmileEntity smile = ModEntities.SMILE.get().create(level);
        if (smile == null) {
            return null;
        }
        smile.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                level.getRandom().nextFloat() * 360.0F, 0.0F);
        smile.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
        smile.setTarget(player);
        level.addFreshEntity(smile);
        level.playSound(null, pos, ModSounds.SMILE_ROAR.get(), SoundSource.HOSTILE, 2.0F, 0.9F);
        return smile;
    }

    @Nullable
    public static TheTruthEntity triggerTheTruthEncounter(ServerLevel level, ServerPlayer player) {
        BlockPos spawnPos = findAheadSpawnPos(level, player);
        if (spawnPos == null) {
            return null;
        }
        TheTruthEntity truth = ModEntities.THE_TRUTH.get().create(level);
        if (truth == null) {
            return null;
        }
        double dx = player.getX() - (spawnPos.getX() + 0.5D);
        double dz = player.getZ() - (spawnPos.getZ() + 0.5D);
        float yaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
        truth.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, yaw, 0.0F);
        truth.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.EVENT, null, null);
        truth.setChasedPlayer(player);
        level.addFreshEntity(truth);
        return truth;
    }

    @Nullable
    public static ClockEntity triggerClockEncounter(ServerLevel level, ServerPlayer player, boolean timeVariant) {
        BlockPos pos;
        if (ModDimensions.isTime(level)) {
            double ang = level.getRandom().nextDouble() * Math.PI * 2.0D;
            int tx = Mth.floor(player.getX() + Math.cos(ang) * 26.0D);
            int tz = Mth.floor(player.getZ() + Math.sin(ang) * 26.0D);
            pos = TimeChunkGenerator.findNearestSafeDiscPos(tx, tz);
        } else {
            pos = findSpawnPosAroundPlayer(level, player,
                    UnderWorldConfigHolder.getSpawnMinDistance(),
                    UnderWorldConfigHolder.getSpawnMaxDistance(),
                    false);
        }
        if (pos == null) {
            return null;
        }
        ClockEntity clock = ModEntities.CLOCK.get().create(level);
        if (clock == null) {
            return null;
        }
        clock.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                level.getRandom().nextFloat() * 360.0F, 0.0F);
        clock.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
        clock.setTimeVariant(timeVariant || ModDimensions.isTime(level));
        level.addFreshEntity(clock);
        return clock;
    }

    @Nullable
    public static TheMimicEntity triggerTheMimicEncounter(ServerLevel level, ServerPlayer player) {
        BlockPos pos = findSpawnPosAroundPlayer(level, player, 18, 34, false);
        if (pos == null) {
            return null;
        }
        TheMimicEntity mimic = ModEntities.THE_MIMIC.get().create(level);
        if (mimic == null) {
            return null;
        }
        mimic.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                level.getRandom().nextFloat() * 360.0F, 0.0F);
        mimic.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
        level.addFreshEntity(mimic);
        return mimic;
    }

    @Nullable
    public static DemonFishEntity triggerDemonFishEncounter(ServerLevel level, ServerPlayer player) {
        BlockPos pos = findWaterSpawnPosAroundPlayer(level, player, 10, 24);
        if (pos == null) {
            pos = findSpawnPosAroundPlayer(level, player, 10, 22, false);
        }
        if (pos == null) {
            return null;
        }
        DemonFishEntity fish = ModEntities.DEMON_FISH.get().create(level);
        if (fish == null) {
            return null;
        }
        fish.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                level.getRandom().nextFloat() * 360.0F, 0.0F);
        fish.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
        fish.setTarget(player);
        level.addFreshEntity(fish);
        level.playSound(null, pos, ModSounds.DEMON_FISH_AMBUSH.get(), SoundSource.HOSTILE, 1.5F, 0.85F);
        return fish;
    }

    @Nullable
    public static TheBallEntity triggerTheBallEncounter(ServerLevel level, ServerPlayer player) {
        BlockPos pos = findSpawnPosAroundPlayer(level, player, 16, 30, false);
        if (pos == null) {
            return null;
        }
        TheBallEntity ball = ModEntities.THE_BALL.get().create(level);
        if (ball == null) {
            return null;
        }
        ball.moveTo(pos.getX() + 0.5D, pos.getY() + 1.8D, pos.getZ() + 0.5D, 0.0F, 0.0F);
        ball.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
        level.addFreshEntity(ball);
        level.playSound(null, pos, ModSounds.THE_BALL_HUM.get(), SoundSource.AMBIENT, 1.3F, 0.85F);
        return ball;
    }

    @Nullable
    public static LivingPylonEntity triggerLivingPylonEncounter(ServerLevel level, ServerPlayer player) {
        BlockPos pos = findSpawnPosAroundPlayer(level, player, 22, 38, false);
        if (pos == null) {
            return null;
        }
        LivingPylonEntity pylon = ModEntities.LIVING_PYLON.get().create(level);
        if (pylon == null) {
            return null;
        }
        pylon.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                level.getRandom().nextFloat() * 360.0F, 0.0F);
        pylon.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
        pylon.setAutoDespawnAtDay(ModDimensions.isPlains(level));
        level.addFreshEntity(pylon);
        level.playSound(null, pos, ModSounds.PYLON_HUM.get(), SoundSource.HOSTILE, 1.8F, 0.8F);
        return pylon;
    }

    @Nullable
    public static WindTurbineStalkerEntity triggerWindTurbineEncounter(ServerLevel level, ServerPlayer player, boolean awakenImmediately) {
        double ang = level.getRandom().nextDouble() * Math.PI * 2.0D;
        double dist = 24.0D + level.getRandom().nextDouble() * 18.0D;
        int tx = Mth.floor(player.getX() + Math.cos(ang) * dist);
        int tz = Mth.floor(player.getZ() + Math.sin(ang) * dist);
        double spawnY = ModDimensions.isWindOcean(level) ? WindOceanChunkGenerator.SEA_LEVEL - 3.0D : player.getY();
        BlockPos pos = new BlockPos(tx, Mth.floor(spawnY), tz);

        WindTurbineStalkerEntity stalker = ModEntities.WIND_TURBINE_STALKER.get().create(level);
        if (stalker == null) {
            return null;
        }
        stalker.moveTo(pos.getX() + 0.5D, spawnY, pos.getZ() + 0.5D,
                level.getRandom().nextFloat() * 360.0F, 0.0F);
        stalker.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
        if (awakenImmediately) {
            stalker.setAwakened(true);
        }
        level.addFreshEntity(stalker);
        return stalker;
    }

    @Nullable
    public static TheDotComEntity triggerDotComEncounter(ServerLevel level, ServerPlayer player) {
        BlockPos pos = findSpawnPosAroundPlayer(level, player, 16, 26, false);
        if (pos == null) {
            pos = player.blockPosition().offset(12, 0, 0);
        }
        TheDotComEntity dotCom = ModEntities.THE_DOT_COM.get().create(level);
        if (dotCom == null) {
            return null;
        }
        dotCom.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                level.getRandom().nextFloat() * 360.0F, 0.0F);
        dotCom.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
        dotCom.setTarget(player);
        level.addFreshEntity(dotCom);
        level.playSound(null, pos, ModSounds.DOT_COM_ROAR.get(), SoundSource.HOSTILE, 2.0F, 0.9F);
        return dotCom;
    }

    public static void triggerDotComTrap(ServerLevel level, BlockPos downloadPos, ServerPlayer player) {
        AABB box = AABB.ofSize(Vec3.atCenterOf(downloadPos), 64.0D, 32.0D, 64.0D);
        if (!level.getEntitiesOfClass(TheDotComEntity.class, box).isEmpty()) {
            return;
        }
        TheDotComEntity dotCom = ModEntities.THE_DOT_COM.get().create(level);
        if (dotCom != null) {
            BlockPos spawnPos = downloadPos.offset(8, 0, 0);
            dotCom.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, 90.0F, 0.0F);
            dotCom.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.EVENT, null, null);
            dotCom.setTarget(player);
            level.addFreshEntity(dotCom);
        }
        level.playSound(null, downloadPos, ModSounds.DOT_COM_ROAR.get(), SoundSource.HOSTILE, 2.4F, 0.85F);
        level.sendParticles(ParticleTypes.CRIMSON_SPORE, downloadPos.getX() + 0.5D, downloadPos.getY() + 1.5D, downloadPos.getZ() + 0.5D,
                40, 2.0D, 1.5D, 2.0D, 0.06D);
        player.displayClientMessage(Component.translatable("message.under_world.dot_com_escaped_left").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), false);
    }

    public static void triggerWelcomeToTheHellEvent(MinecraftServer server, ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos pos = player.blockPosition();

        level.playSound(null, pos, ModSounds.HELL_EVENT.get(), SoundSource.AMBIENT, 2.2F, 0.8F);
        server.getPlayerList().broadcastSystemMessage(
                Component.translatable("message.under_world.welcome_to_the_hell_chat").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                false
        );

        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 25, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 70, 0, false, false));

        for (int i = 0; i < 16; i++) {
            double ang = (Math.PI * 2.0D * i) / 16.0D;
            double px = player.getX() + Math.cos(ang) * 3.5D;
            double pz = player.getZ() + Math.sin(ang) * 3.5D;
            level.sendParticles(ParticleTypes.FLAME, px, player.getY() + 0.4D, pz, 4, 0.2D, 0.5D, 0.2D, 0.04D);
            level.sendParticles(ParticleTypes.CRIMSON_SPORE, px, player.getY() + 1.2D, pz, 5, 0.3D, 0.6D, 0.3D, 0.02D);
        }

        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(player.getX() + 9.0D, player.getY(), player.getZ() + 9.0D);
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }

        Advancement adv = server.getAdvancements().getAdvancement(UnderWorldMod.id("welcome_to_the_hell"));
        if (adv != null) {
            AdvancementProgress progress = player.getAdvancements().getOrStartProgress(adv);
            for (String criterion : progress.getRemainingCriteria()) {
                player.getAdvancements().award(adv, criterion);
            }
        }
    }

    @Nullable
    public static BlockPos findWaterSpawnPosAroundPlayer(ServerLevel level, ServerPlayer player, int minDist, int maxDist) {
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < 32; attempt++) {
            double ang = random.nextDouble() * Math.PI * 2.0D;
            double dist = minDist + random.nextDouble() * (maxDist - minDist);
            int tx = Mth.floor(player.getX() + Math.cos(ang) * dist);
            int tz = Mth.floor(player.getZ() + Math.sin(ang) * dist);
            int surfY = level.getHeight(Heightmap.Types.WORLD_SURFACE, tx, tz);
            for (int dy = 1; dy <= 5; dy++) {
                BlockPos p = new BlockPos(tx, surfY - dy, tz);
                if (level.getFluidState(p).is(FluidTags.WATER)) {
                    return p;
                }
            }
        }
        return null;
    }

    @Nullable
    private static BlockPos findAheadSpawnPos(ServerLevel level, ServerPlayer player) {
        double baseYaw = Math.toRadians(player.getYRot());
        double[] angleOffsets = {0.0D, 0.25D, -0.25D, 0.55D, -0.55D, Math.PI};
        int[] distances = {30, 25, 36, 20};
        boolean surfaceMode = level.canSeeSky(player.blockPosition());

        for (int dist : distances) {
            for (double offset : angleOffsets) {
                double ang = baseYaw + offset;
                double fwdX = -Math.sin(ang);
                double fwdZ = Math.cos(ang);
                int tx = Mth.floor(player.getX() + fwdX * dist);
                int tz = Mth.floor(player.getZ() + fwdZ * dist);

                int y;
                if (surfaceMode) {
                    y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, tx, tz);
                    if (y <= level.getMinBuildHeight() + 2 || y >= level.getMaxBuildHeight() - 52) {
                        y = Mth.clamp(player.blockPosition().getY(), level.getMinBuildHeight() + 2, level.getMaxBuildHeight() - 52);
                    }
                } else {
                    y = Mth.clamp(player.blockPosition().getY(), level.getMinBuildHeight() + 2, level.getMaxBuildHeight() - 52);
                }
                return new BlockPos(tx, y, tz);
            }
        }
        return null;
    }

    private static void syncTruthHudForPlayerDimension(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        List<? extends TheTruthEntity> active = level.getEntities(
                ModEntities.THE_TRUTH.get(),
                e -> e.isAlive() && e.getTruthState() != TheTruthEntity.STATE_VANISH
        );
        if (!active.isEmpty()) {
            TheTruthEntity truth = active.get(0);
            NetworkHandler.syncTruthHudToPlayer(player, true, truth.getRemainingTicks(), truth.getTotalTicks());
        } else {
            NetworkHandler.syncTruthHudToPlayer(player, false, 0, 600);
        }
    }

    private static void trySpawnTruthPillarForPlayer(MinecraftServer server, UUID playerUuid) {
        ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);
        if (player == null || !player.isAlive()) {
            return;
        }
        UnderWorldSavedData data = UnderWorldSavedData.get(server);
        if (!data.consumePendingTruthPillar(playerUuid)) {
            return;
        }
        ServerLevel level = player.serverLevel();
        BlockPos origin = player.blockPosition();
        int minBase = level.getMinBuildHeight() + 2;
        int maxBase = level.getMaxBuildHeight() - 103;
        int baseY = Mth.clamp(origin.getY(), minBase, maxBase);

        for (int dy = 0; dy < 100; dy++) {
            BlockPos p = new BlockPos(origin.getX(), baseY + dy, origin.getZ());
            level.setBlock(p, Blocks.COBBLESTONE.defaultBlockState(), 3);
        }
        BlockPos standPos = new BlockPos(origin.getX(), baseY + 100, origin.getZ());
        level.setBlock(standPos, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(standPos.above(), Blocks.AIR.defaultBlockState(), 3);

        player.fallDistance = 0.0F;
        player.teleportTo(level, standPos.getX() + 0.5D, standPos.getY(), standPos.getZ() + 0.5D,
                player.getYRot(), player.getXRot());
        level.playSound(null, standPos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 2.0F, 0.7F);
    }

    private static void grantSpawnProtection(ServerPlayer player, int ticks) {
        long until = player.serverLevel().getGameTime() + ticks;
        SPAWN_PROTECTION_UNTIL_TICK.put(player.getUUID(), until);
    }

    private static boolean isSpawnProtected(ServerPlayer player) {
        Long until = SPAWN_PROTECTION_UNTIL_TICK.get(player.getUUID());
        return until != null && player.serverLevel().getGameTime() < until;
    }

    private static boolean isUnderWorldEntity(@Nullable Entity entity) {
        if (entity == null) {
            return false;
        }
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return key != null && UnderWorldMod.MOD_ID.equals(key.getNamespace());
    }

    private static boolean shouldVanishOnPlayerDeath(Entity entity, ServerPlayer victim, boolean killedByUnderWorld) {
        if (entity instanceof Mob mob && victim.equals(mob.getTarget())) {
            return true;
        }
        if (entity instanceof FriendEntity) {
            return false;
        }
        if (!killedByUnderWorld) {
            return false;
        }
        if (entity instanceof TreeMimicEntity tree) {
            return tree.isAwakened() && entity.distanceToSqr(victim) <= 32.0D * 32.0D;
        }
        return entity.distanceToSqr(victim) <= 32.0D * 32.0D;
    }

    private static void discardNearbyHostileUnderWorld(ServerLevel level, ServerPlayer player, double radius) {
        AABB box = AABB.ofSize(player.position(), radius * 2.0D, 32.0D, radius * 2.0D);
        for (Entity nearby : level.getEntities(player, box, UnderWorldEventDirector::isUnderWorldEntity)) {
            if (nearby instanceof FriendEntity) {
                continue;
            }
            if (nearby instanceof TreeMimicEntity tree && !tree.isAwakened()) {
                continue;
            }
            if (nearby instanceof TheTruthEntity) {
                NetworkHandler.syncTruthHudToDimension(level, false, 0, 600);
            }
            nearby.discard();
        }
    }

    private static boolean hasActiveTruth(MinecraftServer server) {
        for (ServerLevel lvl : server.getAllLevels()) {
            if (!lvl.getEntities(ModEntities.THE_TRUTH.get(), Entity::isAlive).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static BlockPos findMazeCorridorSpotAround(ServerLevel level, ServerPlayer player, int minDist, int maxDist) {
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < 40; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double dist = minDist + random.nextDouble() * (maxDist - minDist);
            int tx = (int) Math.floor(player.getX() + Math.cos(angle) * dist);
            int tz = (int) Math.floor(player.getZ() + Math.sin(angle) * dist);
            if (!MazeChunkGenerator.isCorridorAir(tx, tz)) {
                continue;
            }
            BlockPos p = new BlockPos(tx, MazeChunkGenerator.FLOOR_Y + 1, tz);
            if (level.getBlockState(p).isAir() && level.getBlockState(p.above()).isAir() && level.getBlockState(p.above(2)).isAir()) {
                return p;
            }
        }
        return null;
    }

    @Nullable
    public static BlockPos findSpawnPosAroundPlayer(ServerLevel level, ServerPlayer player, int minDist, int maxDist, boolean allowCave) {
        RandomSource random = level.getRandom();
        int effectiveMin = Math.max(4, Math.min(minDist, maxDist));
        int effectiveMax = Math.max(effectiveMin, maxDist);

        for (int attempt = 0; attempt < 32; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double dist = effectiveMin + random.nextDouble() * (effectiveMax - effectiveMin);
            int tx = (int) Math.floor(player.getX() + Math.cos(angle) * dist);
            int tz = (int) Math.floor(player.getZ() + Math.sin(angle) * dist);

            if (ModDimensions.isMaze(level)) {
                if (MazeChunkGenerator.isCorridorAir(tx, tz)) {
                    BlockPos p = new BlockPos(tx, MazeChunkGenerator.FLOOR_Y + 1, tz);
                    if (level.getBlockState(p).isAir() && level.getBlockState(p.above()).isAir()) {
                        return p;
                    }
                }
                continue;
            }

            if (allowCave && !level.canSeeSky(player.blockPosition())) {
                int baseY = player.blockPosition().getY();
                for (int dy = 0; dy <= 10; dy++) {
                    int[] offsets = dy == 0 ? new int[]{0} : new int[]{dy, -dy};
                    for (int off : offsets) {
                        BlockPos candidate = new BlockPos(tx, baseY + off, tz);
                        if (isValidCaveSpot(level, candidate)) {
                            return candidate;
                        }
                    }
                }
            }

            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, tx, tz);
            if (surfaceY > level.getMinBuildHeight() + 1 && surfaceY < level.getMaxBuildHeight() - 2) {
                BlockPos surfacePos = new BlockPos(tx, surfaceY, tz);
                if (level.getBlockState(surfacePos).isAir()
                        && level.getBlockState(surfacePos.above()).isAir()
                        && level.getFluidState(surfacePos.below()).isEmpty()) {
                    return surfacePos;
                }
            }
        }
        return null;
    }

    private static boolean isValidCaveSpot(ServerLevel level, BlockPos pos) {
        if (pos.getY() <= level.getMinBuildHeight() + 1 || pos.getY() >= level.getMaxBuildHeight() - 3) {
            return false;
        }
        return level.getBlockState(pos.below()).isSolidRender(level, pos.below())
                && level.getBlockState(pos).isAir()
                && level.getBlockState(pos.above()).isAir()
                && level.getBlockState(pos.above(2)).isAir();
    }

    public static boolean hasFriendInServer(MinecraftServer server, UnderWorldSavedData data) {
        UUID tracked = data.getFriendEntityUuid();
        for (ServerLevel lvl : server.getAllLevels()) {
            if (tracked != null) {
                Entity entity = lvl.getEntity(tracked);
                if (entity instanceof FriendEntity && entity.isAlive()) {
                    return true;
                }
            }
            if (!lvl.getEntities(ModEntities.FRIEND.get(), Entity::isAlive).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasNearbyHorrorEntity(ServerLevel level, Vec3 center, double radius) {
        AABB box = AABB.ofSize(center, radius * 2.0D, radius * 2.0D, radius * 2.0D);
        return !level.getEntitiesOfClass(HandsEntity.class, box).isEmpty()
                || !level.getEntitiesOfClass(TreeMimicEntity.class, box).isEmpty()
                || !level.getEntitiesOfClass(TheSkyEntity.class, box).isEmpty()
                || !level.getEntitiesOfClass(TheTruthEntity.class, box).isEmpty()
                || !level.getEntitiesOfClass(ClockEntity.class, box).isEmpty()
                || !level.getEntitiesOfClass(TheMimicEntity.class, box).isEmpty()
                || !level.getEntitiesOfClass(DemonFishEntity.class, box).isEmpty()
                || !level.getEntitiesOfClass(TheBallEntity.class, box).isEmpty()
                || !level.getEntitiesOfClass(LivingPylonEntity.class, box).isEmpty()
                || !level.getEntitiesOfClass(WindTurbineStalkerEntity.class, box).isEmpty()
                || !level.getEntitiesOfClass(TheDotComEntity.class, box).isEmpty();
    }

    @Nullable
    private static ServerPlayer pickSurvivalPlayer(ServerLevel level) {
        List<ServerPlayer> candidates = level.getPlayers(p -> !p.isCreative() && !p.isSpectator() && p.isAlive());
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(level.getRandom().nextInt(candidates.size()));
    }

    @Nullable
    private static ServerPlayer pickAnyPlayer(ServerLevel level) {
        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) {
            return null;
        }
        return players.get(level.getRandom().nextInt(players.size()));
    }
}
