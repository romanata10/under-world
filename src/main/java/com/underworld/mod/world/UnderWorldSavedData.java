package com.underworld.mod.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
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
}
