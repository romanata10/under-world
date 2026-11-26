package com.underworld.mod.client;

import com.underworld.mod.client.renderer.FriendRenderer;
import com.underworld.mod.client.renderer.HandsRenderer;
import com.underworld.mod.client.renderer.TheSkyRenderer;
import com.underworld.mod.client.renderer.TreeMimicRenderer;
import com.underworld.mod.registry.ModEntities;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public final class ClientSetup {
    private ClientSetup() {}

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(ClientSetup::onRegisterRenderers);
    }

    private static void onRegisterRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.FRIEND.get(), FriendRenderer::new);
        event.registerEntityRenderer(ModEntities.HANDS.get(), HandsRenderer::new);
        event.registerEntityRenderer(ModEntities.TREE_MIMIC.get(), TreeMimicRenderer::new);
        event.registerEntityRenderer(ModEntities.THE_SKY.get(), TheSkyRenderer::new);
    }
}
