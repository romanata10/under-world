package com.underworld.mod.registry;

import com.underworld.mod.UnderWorldMod;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, UnderWorldMod.MOD_ID);

    public static final RegistryObject<SoundEvent> HANDS_SCREAM = register("hands_scream");
    public static final RegistryObject<SoundEvent> HANDS_GLITCH = register("hands_glitch");
    public static final RegistryObject<SoundEvent> HANDS_BURROW = register("hands_burrow");
    public static final RegistryObject<SoundEvent> TREE_AWAKEN = register("tree_awaken");
    public static final RegistryObject<SoundEvent> TREE_STEP = register("tree_step");
    public static final RegistryObject<SoundEvent> THE_SKY_ROAR = register("the_sky_roar");
    public static final RegistryObject<SoundEvent> THE_SKY_STOMP = register("the_sky_stomp");

    private ModSounds() {}

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(UnderWorldMod.id(name)));
    }
}
