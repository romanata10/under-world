package com.underworld.mod.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
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

    @SubscribeEvent
    public static void onScreenInitPost(final ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof PauseScreen pause) {
            int w = 58;
            event.addListener(Button.builder(Component.translatable("wiki.under_world.button"), btn -> {
                Minecraft mc = Minecraft.getInstance();
                mc.setScreen(new UnderWorldWikiScreen(pause));
            }).bounds(pause.width - w - 8, 8, w, 20).build());
        }
    }
}
