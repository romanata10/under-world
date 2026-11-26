package com.underworld.mod.network;

import com.underworld.mod.client.ClientFunWorldTimeHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FunWorldTimeSyncPacket {
    private final long dayTime;
    private final int speedMultiplier;

    public FunWorldTimeSyncPacket(long dayTime, int speedMultiplier) {
        this.dayTime = dayTime;
        this.speedMultiplier = speedMultiplier;
    }

    public static void encode(FunWorldTimeSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.dayTime);
        buffer.writeVarInt(packet.speedMultiplier);
    }

    public static FunWorldTimeSyncPacket decode(FriendlyByteBuf buffer) {
        long dayTime = buffer.readLong();
        int speed = buffer.readVarInt();
        return new FunWorldTimeSyncPacket(dayTime, speed);
    }

    public static void handle(FunWorldTimeSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        ClientFunWorldTimeHandler.handleSync(packet.dayTime, packet.speedMultiplier))
        );
        context.setPacketHandled(true);
    }
}
