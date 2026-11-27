package com.underworld.mod.client.gui;

import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ClientMenuHandler {
    private ClientMenuHandler() {}

    @SubscribeEvent
    public static void onScreenOpening(final ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof TitleScreen && !(event.getNewScreen() instanceof UnderWorldTitleScreen)) {
            event.setNewScreen(new UnderWorldTitleScreen());
        }
    }
}
