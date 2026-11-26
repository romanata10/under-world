package com.underworld.mod.client;

import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.client.dimension.DimensionAtmosphereHandler;
import com.underworld.mod.client.dimension.FunWorldDimensionEffects;
import com.underworld.mod.client.dimension.MazeDimensionEffects;
import com.underworld.mod.client.hud.TheTruthHudOverlay;
import com.underworld.mod.client.renderer.EyesOfFunRenderer;
import com.underworld.mod.client.renderer.FriendRenderer;
import com.underworld.mod.client.renderer.FunFlowerBlockRenderer;
import com.underworld.mod.client.renderer.HandsRenderer;
import com.underworld.mod.client.renderer.SmileRenderer;
import com.underworld.mod.client.renderer.TheSkyRenderer;
import com.underworld.mod.client.renderer.TheTruthRenderer;
import com.underworld.mod.client.renderer.TreeMimicRenderer;
import com.underworld.mod.registry.ModBlockEntities;
import com.underworld.mod.registry.ModEntities;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

public final class ClientSetup {
    private ClientSetup() {}

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(ClientSetup::onRegisterRenderers);
        modEventBus.addListener(ClientSetup::onRegisterDimensionEffects);
        modEventBus.addListener(ClientSetup::onRegisterGuiOverlays);
        MinecraftForge.EVENT_BUS.register(DimensionAtmosphereHandler.class);
        MinecraftForge.EVENT_BUS.register(ClientConnectionHandler.class);
    }

    private static void onRegisterRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.FRIEND.get(), FriendRenderer::new);
        event.registerEntityRenderer(ModEntities.HANDS.get(), HandsRenderer::new);
        event.registerEntityRenderer(ModEntities.TREE_MIMIC.get(), TreeMimicRenderer::new);
        event.registerEntityRenderer(ModEntities.THE_SKY.get(), TheSkyRenderer::new);
        event.registerEntityRenderer(ModEntities.EYES_OF_FUN.get(), EyesOfFunRenderer::new);
        event.registerEntityRenderer(ModEntities.SMILE.get(), SmileRenderer::new);
        event.registerEntityRenderer(ModEntities.THE_TRUTH.get(), TheTruthRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.FUN_FLOWER.get(), context -> new FunFlowerBlockRenderer());
    }

    private static void onRegisterDimensionEffects(final RegisterDimensionSpecialEffectsEvent event) {
        event.register(UnderWorldMod.id("the_fun_world"), new FunWorldDimensionEffects());
        event.register(UnderWorldMod.id("maze"), new MazeDimensionEffects());
    }

    private static void onRegisterGuiOverlays(final RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("the_truth_overlay", TheTruthHudOverlay::render);
    }
}
