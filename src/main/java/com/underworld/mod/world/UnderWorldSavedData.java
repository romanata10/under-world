package com.underworld.mod.world;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class UnderWorldSavedData extends SavedData {
    private static final String DATA_NAME = "under_world_data";

    private boolean friendHasJoined = false;
    private boolean friendInTabList = false;
    @Nullable
    private UUID friendEntityUuid = null;
    private boolean eventsPaused = false;
    private int eventCooldownTicks = 600;
    private int ambientChatTimerTicks = 1200;
    private int friendStalkCooldownTicks = 900;
    private int truthCooldownTicks = 12000;
    private int structureEventCooldownTicks = 1200;
    private long funWorldDayTime = 1000L;
    private final Map<UUID, BlockPos> returnPositions = new HashMap<>();
    private final Map<UUID, Integer> errorReturnTimers = new HashMap<>();
    private final Set<UUID> pendingTruthPillars = new HashSet<>();
    private final Set<String> spawnedLoreStructures = new HashSet<>();

    public UnderWorldSavedData() {}

    public static UnderWorldSavedData get(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return new UnderWorldSavedData();
        }
        return overworld.getDataStorage().computeIfAbsent(
                UnderWorldSavedData::load,
                UnderWorldSavedData::new,
                DATA_NAME
        );
    }

    public static UnderWorldSavedData get(ServerLevel level) {
        return get(level.getServer());
    }

    public static UnderWorldSavedData load(CompoundTag tag) {
        UnderWorldSavedData data = new UnderWorldSavedData();
        data.friendHasJoined = tag.getBoolean("FriendHasJoined");
        data.friendInTabList = tag.getBoolean("FriendInTabList");
        if (tag.hasUUID("FriendEntityUuid")) {
            data.friendEntityUuid = tag.getUUID("FriendEntityUuid");
        }
        data.eventsPaused = tag.getBoolean("EventsPaused");
        data.eventCooldownTicks = tag.contains("EventCooldownTicks") ? tag.getInt("EventCooldownTicks") : 600;
        data.ambientChatTimerTicks = tag.contains("AmbientChatTimerTicks") ? tag.getInt("AmbientChatTimerTicks") : 1200;
        data.friendStalkCooldownTicks = tag.contains("FriendStalkCooldownTicks") ? tag.getInt("FriendStalkCooldownTicks") : 900;
        data.truthCooldownTicks = tag.contains("TruthCooldownTicks") ? tag.getInt("TruthCooldownTicks") : 12000;
        data.structureEventCooldownTicks = tag.contains("StructureEventCooldownTicks") ? tag.getInt("StructureEventCooldownTicks") : 1200;
        data.funWorldDayTime = tag.contains("FunWorldDayTime") ? tag.getLong("FunWorldDayTime") : 1000L;

        if (tag.contains("ReturnPositions", 10)) {
            CompoundTag returns = tag.getCompound("ReturnPositions");
            for (String key : returns.getAllKeys()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    CompoundTag posTag = returns.getCompound(key);
                    data.returnPositions.put(uuid, new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z")));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        if (tag.contains("ErrorReturnTimers", 10)) {
            CompoundTag timers = tag.getCompound("ErrorReturnTimers");
            for (String key : timers.getAllKeys()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    int ticks = timers.getInt(key);
                    if (ticks > 0) {
                        data.errorReturnTimers.put(uuid, ticks);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        if (tag.contains("PendingTruthPillars", Tag.TAG_LIST)) {
            ListTag list = tag.getList("PendingTruthPillars", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                try {
                    data.pendingTruthPillars.add(UUID.fromString(list.getString(i)));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        if (tag.contains("SpawnedLoreStructures", Tag.TAG_LIST)) {
            ListTag loreList = tag.getList("SpawnedLoreStructures", Tag.TAG_STRING);
            for (int i = 0; i < loreList.size(); i++) {
                data.spawnedLoreStructures.add(loreList.getString(i));
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("FriendHasJoined", this.friendHasJoined);
        tag.putBoolean("FriendInTabList", this.friendInTabList);
        if (this.friendEntityUuid != null) {
            tag.putUUID("FriendEntityUuid", this.friendEntityUuid);
        }
        tag.putBoolean("EventsPaused", this.eventsPaused);
        tag.putInt("EventCooldownTicks", this.eventCooldownTicks);
        tag.putInt("AmbientChatTimerTicks", this.ambientChatTimerTicks);
        tag.putInt("FriendStalkCooldownTicks", this.friendStalkCooldownTicks);
        tag.putInt("TruthCooldownTicks", this.truthCooldownTicks);
        tag.putInt("StructureEventCooldownTicks", this.structureEventCooldownTicks);
        tag.putLong("FunWorldDayTime", this.funWorldDayTime);

        CompoundTag returns = new CompoundTag();
        for (Map.Entry<UUID, BlockPos> entry : this.returnPositions.entrySet()) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", entry.getValue().getX());
            posTag.putInt("y", entry.getValue().getY());
            posTag.putInt("z", entry.getValue().getZ());
            returns.put(entry.getKey().toString(), posTag);
        }
        tag.put("ReturnPositions", returns);

        CompoundTag timers = new CompoundTag();
        for (Map.Entry<UUID, Integer> entry : this.errorReturnTimers.entrySet()) {
            if (entry.getValue() > 0) {
                timers.putInt(entry.getKey().toString(), entry.getValue());
            }
        }
        tag.put("ErrorReturnTimers", timers);

        ListTag pendingPillars = new ListTag();
        for (UUID uuid : this.pendingTruthPillars) {
            pendingPillars.add(StringTag.valueOf(uuid.toString()));
        }
        tag.put("PendingTruthPillars", pendingPillars);

        ListTag loreList = new ListTag();
        for (String loreKey : this.spawnedLoreStructures) {
            loreList.add(StringTag.valueOf(loreKey));
        }
        tag.put("SpawnedLoreStructures", loreList);
        return tag;
    }

    public boolean hasFriendJoined() {
        return this.friendHasJoined;
    }

    public void setFriendHasJoined(boolean friendHasJoined) {
        this.friendHasJoined = friendHasJoined;
        setDirty();
    }

    public boolean isFriendInTabList() {
        return this.friendInTabList;
    }

    public void setFriendInTabList(boolean friendInTabList) {
        this.friendInTabList = friendInTabList;
        setDirty();
    }

    @Nullable
    public UUID getFriendEntityUuid() {
        return this.friendEntityUuid;
    }

    public void setFriendEntityUuid(@Nullable UUID friendEntityUuid) {
        this.friendEntityUuid = friendEntityUuid;
        setDirty();
    }

    public boolean areEventsPaused() {
        return this.eventsPaused;
    }

    public void setEventsPaused(boolean eventsPaused) {
        this.eventsPaused = eventsPaused;
        setDirty();
    }

    public int getEventCooldownTicks() {
        return this.eventCooldownTicks;
    }

    public void setEventCooldownTicks(int eventCooldownTicks) {
        this.eventCooldownTicks = Math.max(0, eventCooldownTicks);
        setDirty();
    }

    public int getAmbientChatTimerTicks() {
        return this.ambientChatTimerTicks;
    }

    public void setAmbientChatTimerTicks(int ambientChatTimerTicks) {
        this.ambientChatTimerTicks = Math.max(0, ambientChatTimerTicks);
        setDirty();
    }

    public int getFriendStalkCooldownTicks() {
        return this.friendStalkCooldownTicks;
    }

    public void setFriendStalkCooldownTicks(int ticks) {
        this.friendStalkCooldownTicks = Math.max(0, ticks);
        setDirty();
    }

    public int getTruthCooldownTicks() {
        return this.truthCooldownTicks;
    }

    public void setTruthCooldownTicks(int ticks) {
        this.truthCooldownTicks = Math.max(0, ticks);
        setDirty();
    }

    public long getFunWorldDayTime() {
        return this.funWorldDayTime;
    }

    public void setFunWorldDayTime(long funWorldDayTime) {
        this.funWorldDayTime = Math.max(0L, funWorldDayTime);
        setDirty();
    }

    public void markTruthPillarPending(UUID playerId) {
        this.pendingTruthPillars.add(playerId);
        setDirty();
    }

    public boolean hasPendingTruthPillar(UUID playerId) {
        return this.pendingTruthPillars.contains(playerId);
    }

    public boolean consumePendingTruthPillar(UUID playerId) {
        boolean removed = this.pendingTruthPillars.remove(playerId);
        if (removed) {
            setDirty();
        }
        return removed;
    }

    public void saveReturnPos(UUID playerId, BlockPos pos) {
        this.returnPositions.put(playerId, pos.immutable());
        setDirty();
    }

    @Nullable
    public BlockPos getReturnPos(UUID playerId) {
        return this.returnPositions.get(playerId);
    }

    public void setErrorReturnTimer(UUID playerId, int ticks) {
        if (ticks <= 0) {
            this.errorReturnTimers.remove(playerId);
        } else {
            this.errorReturnTimers.put(playerId, ticks);
        }
        setDirty();
    }

    public int getErrorReturnTimer(UUID playerId) {
        return this.errorReturnTimers.getOrDefault(playerId, 0);
    }

    public void clearErrorReturnTimer(UUID playerId) {
        if (this.errorReturnTimers.remove(playerId) != null) {
            setDirty();
        }
    }

    public int getStructureEventCooldownTicks() {
        return this.structureEventCooldownTicks;
    }

    public void setStructureEventCooldownTicks(int ticks) {
        this.structureEventCooldownTicks = Math.max(0, ticks);
        setDirty();
    }

    public boolean hasLoreStructureSpawned(String key) {
        return this.spawnedLoreStructures.contains(key);
    }

    public void markLoreStructureSpawned(String key) {
        if (this.spawnedLoreStructures.add(key)) {
            setDirty();
        }
    }
}
