package com.underworld.mod.event;

import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.config.UnderWorldConfigHolder;
import com.underworld.mod.entity.FriendEntity;
import com.underworld.mod.entity.HandsEntity;
import com.underworld.mod.entity.TheSkyEntity;
import com.underworld.mod.entity.TreeMimicEntity;
import com.underworld.mod.network.NetworkHandler;
import com.underworld.mod.registry.ModEntities;
import com.underworld.mod.world.UnderWorldSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class UnderWorldEventDirector {
    private static final String[] AMBIENT_FRIEND_MESSAGES = {
            "hello?",
            "im your friend",
            "the void"
    };

    private static final List<ScheduledAction> SCHEDULED_ACTIONS = new ArrayList<>();
    private static final List<PendingKick> PENDING_KICKS = new ArrayList<>();

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

        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }

        UnderWorldSavedData data = UnderWorldSavedData.get(server);
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

        // 3. Rare automatic encounters checked every 5 seconds (100 ticks)
        int cooldown = data.getEventCooldownTicks();
        if (cooldown > 0) {
            data.setEventCooldownTicks(cooldown - 1);
            return;
        }

        if (server.getTickCount() % 100 != 0) {
            return;
        }

        ServerPlayer target = pickSurvivalPlayer(overworld);
        if (target == null) {
            return;
        }

        if (hasNearbyHorrorEntity(overworld, target.position(), 64.0D)) {
            return;
        }

        RandomSource random = overworld.getRandom();
        BlockPos playerPos = target.blockPosition();
        boolean seeSky = overworld.canSeeSky(playerPos);

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

        // Surface encounters: The Sky (super rare) or Tree? (rare)
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

            if (UnderWorldConfigHolder.get().treeMimic.treeMimicEnabled.get()) {
                Holder<Biome> biome = overworld.getBiome(playerPos);
                boolean wooded = biome.is(BiomeTags.IS_FOREST) || biome.is(BiomeTags.IS_TAIGA);
                double treeChance = UnderWorldConfigHolder.get().treeMimic.treeMimicSpawnChance.get() * (wooded ? 0.22D : 0.08D);
                if (random.nextDouble() < treeChance) {
                    if (triggerTreeEncounter(overworld, target) != null) {
                        data.setEventCooldownTicks(3600 + random.nextInt(3600));
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
                BlockPos pos = findSpawnPosAroundPlayer(level, anchor,
                        UnderWorldConfigHolder.getSpawnMinDistance(),
                        UnderWorldConfigHolder.getSpawnMaxDistance(),
                        true);
                if (pos == null) {
                    pos = anchor.blockPosition().offset(16, 0, 0);
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
        }

        return true;
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
    public static BlockPos findSpawnPosAroundPlayer(ServerLevel level, ServerPlayer player, int minDist, int maxDist, boolean allowCave) {
        RandomSource random = level.getRandom();
        int effectiveMin = Math.max(4, Math.min(minDist, maxDist));
        int effectiveMax = Math.max(effectiveMin, maxDist);

        for (int attempt = 0; attempt < 32; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double dist = effectiveMin + random.nextDouble() * (effectiveMax - effectiveMin);
            int tx = (int) Math.floor(player.getX() + Math.cos(angle) * dist);
            int tz = (int) Math.floor(player.getZ() + Math.sin(angle) * dist);

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
                || !level.getEntitiesOfClass(TheSkyEntity.class, box).isEmpty();
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
