package com.underworld.mod.client;

import com.underworld.mod.client.hud.ClientTruthHudState;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ClientConnectionHandler {
    private ClientConnectionHandler() {}

    @SubscribeEvent
    public static void onClientLogout(final ClientPlayerNetworkEvent.LoggingOut event) {
        ClientTruthHudState.reset();
        ClientFunWorldTimeHandler.reset();
    }
}
