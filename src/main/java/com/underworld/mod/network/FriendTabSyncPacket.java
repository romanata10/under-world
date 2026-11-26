package com.underworld.mod.network;

import com.underworld.mod.client.ClientFriendTabHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class FriendTabSyncPacket {
    private final boolean listed;
    private final UUID uuid;
    private final String displayName;

    public FriendTabSyncPacket(boolean listed, UUID uuid, String displayName) {
        this.listed = listed;
        this.uuid = uuid;
        this.displayName = displayName;
    }

    public static void encode(FriendTabSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.listed);
        buffer.writeUUID(packet.uuid);
        buffer.writeUtf(packet.displayName, 32);
    }

    public static FriendTabSyncPacket decode(FriendlyByteBuf buffer) {
        boolean listed = buffer.readBoolean();
        UUID uuid = buffer.readUUID();
        String name = buffer.readUtf(32);
        return new FriendTabSyncPacket(listed, uuid, name);
    }

    public static void handle(FriendTabSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        ClientFriendTabHandler.handleSync(packet.listed, packet.uuid, packet.displayName))
        );
        context.setPacketHandled(true);
    }
}
