package com.underworld.mod;

import com.mojang.logging.LogUtils;
import com.underworld.mod.client.ClientSetup;
import com.underworld.mod.command.UnderWorldCommands;
import com.underworld.mod.config.UnderWorldConfigHolder;
import com.underworld.mod.event.FriendChatHandler;
import com.underworld.mod.event.UnderWorldEventDirector;
import com.underworld.mod.network.NetworkHandler;
import com.underworld.mod.registry.ModBlockEntities;
import com.underworld.mod.registry.ModBlocks;
import com.underworld.mod.registry.ModChunkGenerators;
import com.underworld.mod.registry.ModEffects;
import com.underworld.mod.registry.ModEntities;
import com.underworld.mod.registry.ModItems;
import com.underworld.mod.registry.ModSounds;
import com.underworld.mod.registry.ModStructures;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;

@Mod(UnderWorldMod.MOD_ID)
public final class UnderWorldMod {
    public static final String MOD_ID = "under_world";
    public static final Logger LOGGER = LogUtils.getLogger();

    public UnderWorldMod() {
        GeckoLib.initialize();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModEffects.MOB_EFFECTS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModItems.CREATIVE_TABS.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModStructures.STRUCTURE_TYPES.register(modEventBus);
        ModStructures.STRUCTURE_PIECES.register(modEventBus);
        ModChunkGenerators.CHUNK_GENERATORS.register(modEventBus);

        UnderWorldConfigHolder.init(modEventBus);

        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(ModEntities::registerAttributes);
        modEventBus.addListener(ModItems::addCreativeTabItems);

        MinecraftForge.EVENT_BUS.register(UnderWorldEventDirector.class);
        MinecraftForge.EVENT_BUS.register(FriendChatHandler.class);
        MinecraftForge.EVENT_BUS.register(UnderWorldCommands.class);

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientSetup.init(modEventBus));
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::register);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
