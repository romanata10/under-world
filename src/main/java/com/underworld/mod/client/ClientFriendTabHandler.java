package com.underworld.mod.client;

import com.mojang.authlib.GameProfile;
import com.underworld.mod.mixin.ClientPacketListenerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ClientFriendTabHandler {
    private ClientFriendTabHandler() {}

    public static void handleSync(boolean listed, UUID uuid, String name) {
        Minecraft mc = Minecraft.getInstance();
        ClientPacketListener listener = mc.getConnection();
        if (listener == null) {
            return;
        }
        ClientPacketListenerAccessor accessor = (ClientPacketListenerAccessor) listener;
        Map<UUID, PlayerInfo> map = accessor.underWorld$getPlayerInfoMap();
        Set<PlayerInfo> listedSet = accessor.underWorld$getListedPlayers();

        if (!listed) {
            PlayerInfo existing = map.remove(uuid);
            if (existing != null) {
                listedSet.remove(existing);
            }
            return;
        }

        PlayerInfo existing = map.get(uuid);
        if (existing != null) {
            existing.setTabListDisplayName(Component.literal(name));
            listedSet.add(existing);
            return;
        }

        GameProfile profile = new GameProfile(uuid, name);
        PlayerInfo info = new PlayerInfo(profile, false);
        info.setTabListDisplayName(Component.literal(name));
        map.put(uuid, info);
        listedSet.add(info);
    }
}
