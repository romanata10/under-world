package com.underworld.mod.network;

import com.underworld.mod.UnderWorldMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class NetworkHandler {
    private static final String PROTOCOL_VERSION = "2";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            UnderWorldMod.id("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private NetworkHandler() {}

    public static void register() {
        CHANNEL.registerMessage(
                packetId++,
                FriendTabSyncPacket.class,
                FriendTabSyncPacket::encode,
                FriendTabSyncPacket::decode,
                FriendTabSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                packetId++,
                FunWorldTimeSyncPacket.class,
                FunWorldTimeSyncPacket::encode,
                FunWorldTimeSyncPacket::decode,
                FunWorldTimeSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                packetId++,
                TruthHudSyncPacket.class,
                TruthHudSyncPacket::encode,
                TruthHudSyncPacket::decode,
                TruthHudSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    public static void syncFriendTabToPlayer(ServerPlayer player, boolean listed, String displayName) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new FriendTabSyncPacket(listed, FriendTabHelper.FRIEND_UUID, displayName)
        );
    }

    public static void syncFriendTabToAll(boolean listed, String displayName) {
        CHANNEL.send(
                PacketDistributor.ALL.noArg(),
                new FriendTabSyncPacket(listed, FriendTabHelper.FRIEND_UUID, displayName)
        );
    }

    public static void syncFunWorldTimeToPlayer(ServerPlayer player, long dayTime, int speedMultiplier) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new FunWorldTimeSyncPacket(dayTime, speedMultiplier)
        );
    }

    public static void syncTruthHudToPlayer(ServerPlayer player, boolean active, int remainingTicks, int totalTicks) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new TruthHudSyncPacket(active, remainingTicks, totalTicks)
        );
    }

    public static void syncTruthHudToDimension(ServerLevel level, boolean active, int remainingTicks, int totalTicks) {
        CHANNEL.send(
                PacketDistributor.DIMENSION.with(level::dimension),
                new TruthHudSyncPacket(active, remainingTicks, totalTicks)
        );
    }
}
