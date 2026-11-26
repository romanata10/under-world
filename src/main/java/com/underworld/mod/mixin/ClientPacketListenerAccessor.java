package com.underworld.mod.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mixin(ClientPacketListener.class)
public interface ClientPacketListenerAccessor {

    @Accessor("playerInfoMap")
    Map<UUID, PlayerInfo> underWorld$getPlayerInfoMap();

    @Accessor("listedPlayers")
    Set<PlayerInfo> underWorld$getListedPlayers();
}
