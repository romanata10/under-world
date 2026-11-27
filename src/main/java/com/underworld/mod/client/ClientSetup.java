package com.underworld.mod.client;

import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.client.dimension.BrowserDimensionEffects;
import com.underworld.mod.client.dimension.ClientHorrorEffectsHandler;
import com.underworld.mod.client.dimension.DimensionAtmosphereHandler;
import com.underworld.mod.client.dimension.DimensionMusicHandler;
import com.underworld.mod.client.dimension.ErrorDimensionEffects;
import com.underworld.mod.client.dimension.FunWorldDimensionEffects;
import com.underworld.mod.client.dimension.MazeDimensionEffects;
import com.underworld.mod.client.dimension.PlainsDimensionEffects;
import com.underworld.mod.client.dimension.TimeDimensionEffects;
import com.underworld.mod.client.dimension.WindOceanDimensionEffects;
import com.underworld.mod.client.dimension.XpDimensionEffects;
import com.underworld.mod.client.gui.ClientMenuHandler;
import com.underworld.mod.client.hud.GeometricNightmareOverlay;
import com.underworld.mod.client.hud.TheTruthHudOverlay;
import com.underworld.mod.client.renderer.ClockRenderer;
import com.underworld.mod.client.renderer.DemonFishRenderer;
import com.underworld.mod.client.renderer.EyesOfFunRenderer;
import com.underworld.mod.client.renderer.FriendRenderer;
import com.underworld.mod.client.renderer.FunFlowerBlockRenderer;
import com.underworld.mod.client.renderer.GlitchBlockRenderer;
import com.underworld.mod.client.renderer.HandsRenderer;
import com.underworld.mod.client.renderer.LivingPylonRenderer;
import com.underworld.mod.client.renderer.SmileRenderer;
import com.underworld.mod.client.renderer.TheBallRenderer;
import com.underworld.mod.client.renderer.TheDotComRenderer;
import com.underworld.mod.client.renderer.TheMimicRenderer;
import com.underworld.mod.client.renderer.TheSkyRenderer;
import com.underworld.mod.client.renderer.TheTruthRenderer;
import com.underworld.mod.client.renderer.TreeMimicRenderer;
import com.underworld.mod.client.renderer.WindTurbineStalkerRenderer;
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
        MinecraftForge.EVENT_BUS.register(DimensionMusicHandler.class);
        MinecraftForge.EVENT_BUS.register(ClientConnectionHandler.class);
        MinecraftForge.EVENT_BUS.register(ClientHorrorEffectsHandler.class);
        MinecraftForge.EVENT_BUS.register(ClientMenuHandler.class);
    }

    private static void onRegisterRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.FRIEND.get(), FriendRenderer::new);
        event.registerEntityRenderer(ModEntities.HANDS.get(), HandsRenderer::new);
        event.registerEntityRenderer(ModEntities.TREE_MIMIC.get(), TreeMimicRenderer::new);
        event.registerEntityRenderer(ModEntities.THE_SKY.get(), TheSkyRenderer::new);
        event.registerEntityRenderer(ModEntities.EYES_OF_FUN.get(), EyesOfFunRenderer::new);
        event.registerEntityRenderer(ModEntities.SMILE.get(), SmileRenderer::new);
        event.registerEntityRenderer(ModEntities.THE_TRUTH.get(), TheTruthRenderer::new);
        event.registerEntityRenderer(ModEntities.CLOCK.get(), ClockRenderer::new);
        event.registerEntityRenderer(ModEntities.THE_MIMIC.get(), TheMimicRenderer::new);
        event.registerEntityRenderer(ModEntities.DEMON_FISH.get(), DemonFishRenderer::new);
        event.registerEntityRenderer(ModEntities.THE_BALL.get(), TheBallRenderer::new);
        event.registerEntityRenderer(ModEntities.LIVING_PYLON.get(), LivingPylonRenderer::new);
        event.registerEntityRenderer(ModEntities.WIND_TURBINE_STALKER.get(), WindTurbineStalkerRenderer::new);
        event.registerEntityRenderer(ModEntities.THE_DOT_COM.get(), TheDotComRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.FUN_FLOWER.get(), context -> new FunFlowerBlockRenderer());
        event.registerBlockEntityRenderer(ModBlockEntities.GLITCH_BLOCK.get(), GlitchBlockRenderer::new);
    }

    private static void onRegisterDimensionEffects(final RegisterDimensionSpecialEffectsEvent event) {
        event.register(UnderWorldMod.id("the_fun_world"), new FunWorldDimensionEffects());
        event.register(UnderWorldMod.id("maze"), new MazeDimensionEffects());
        event.register(UnderWorldMod.id("the_error"), new ErrorDimensionEffects());
        event.register(UnderWorldMod.id("the_time"), new TimeDimensionEffects());
        event.register(UnderWorldMod.id("the_plains"), new PlainsDimensionEffects());
        event.register(UnderWorldMod.id("the_wind_ocean"), new WindOceanDimensionEffects());
        event.register(UnderWorldMod.id("the_xp"), new XpDimensionEffects());
        event.register(UnderWorldMod.id("the_browser"), new BrowserDimensionEffects());
    }

    private static void onRegisterGuiOverlays(final RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("the_truth_overlay", TheTruthHudOverlay::render);
        event.registerAboveAll("geometric_nightmare_overlay", GeometricNightmareOverlay::render);
    }
}
