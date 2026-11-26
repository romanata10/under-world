package com.underworld.mod.network;

import com.underworld.mod.client.hud.ClientTruthHudState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TruthHudSyncPacket {
    private final boolean active;
    private final int remainingTicks;
    private final int totalTicks;

    public TruthHudSyncPacket(boolean active, int remainingTicks, int totalTicks) {
        this.active = active;
        this.remainingTicks = remainingTicks;
        this.totalTicks = totalTicks;
    }

    public static void encode(TruthHudSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.active);
        buffer.writeVarInt(packet.remainingTicks);
        buffer.writeVarInt(packet.totalTicks);
    }

    public static TruthHudSyncPacket decode(FriendlyByteBuf buffer) {
        boolean active = buffer.readBoolean();
        int remaining = buffer.readVarInt();
        int total = buffer.readVarInt();
        return new TruthHudSyncPacket(active, remaining, total);
    }

    public static void handle(TruthHudSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        ClientTruthHudState.applySync(packet.active, packet.remainingTicks, packet.totalTicks))
        );
        context.setPacketHandled(true);
    }
}
