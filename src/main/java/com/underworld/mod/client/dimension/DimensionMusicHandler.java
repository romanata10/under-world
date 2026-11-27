package com.underworld.mod.client.dimension;

import com.underworld.mod.config.UnderWorldConfigHolder;
import com.underworld.mod.registry.ModDimensions;
import com.underworld.mod.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public final class DimensionMusicHandler {
    @Nullable
    private static ResourceKey<Level> lastDimension = null;

    private DimensionMusicHandler() {}

    @SubscribeEvent
    public static void onClientTick(final TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            lastDimension = null;
            return;
        }
        ResourceKey<Level> current = level.dimension();
        if (!current.equals(lastDimension)) {
            lastDimension = current;
            if (UnderWorldConfigHolder.isCustomMusicEnabled()) {
                mc.getMusicManager().stopPlaying();
            }
        }
    }

    @Nullable
    public static Music resolveCustomMusic(@Nullable ClientLevel level) {
        if (level == null || !UnderWorldConfigHolder.isCustomMusicEnabled()) {
            return null;
        }
        if (ModDimensions.isFunWorld(level)) {
            return createMusic(ModSounds.MUSIC_FUN_WORLD.get(), 40, 220);
        }
        if (ModDimensions.isMaze(level)) {
            return createMusic(ModSounds.MUSIC_MAZE.get(), 40, 220);
        }
        if (ModDimensions.isError(level)) {
            return createMusic(ModSounds.MUSIC_THE_ERROR.get(), 40, 220);
        }
        if (ModDimensions.isTime(level)) {
            return createMusic(ModSounds.MUSIC_THE_TIME.get(), 40, 220);
        }
        if (level.dimension().equals(Level.OVERWORLD) && UnderWorldConfigHolder.isOverworldCustomMusicEnabled()) {
            return createMusic(ModSounds.MUSIC_OVERWORLD.get(), 120, 600);
        }
        return null;
    }

    private static Music createMusic(SoundEvent event, int minDelay, int maxDelay) {
        Holder<SoundEvent> holder = ForgeRegistries.SOUND_EVENTS.getHolder(event)
                .orElseGet(() -> Holder.direct(event));
        return new Music(holder, minDelay, maxDelay, true);
    }
}
