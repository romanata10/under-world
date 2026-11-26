package com.underworld.mod.network;

import com.underworld.mod.UnderWorldMod;
import net.minecraft.resources.ResourceLocation;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class FriendTabHelper {
    public static final UUID FRIEND_UUID = UUID.nameUUIDFromBytes("under_world:friend".getBytes(StandardCharsets.UTF_8));
    public static final ResourceLocation FRIEND_TAB_SKIN = UnderWorldMod.id("textures/entity/friend/friend_tab.png");

    private FriendTabHelper() {}
}
