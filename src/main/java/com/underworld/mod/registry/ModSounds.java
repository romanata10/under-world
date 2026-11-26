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

    public static final RegistryObject<SoundEvent> FRIEND_VANISH = register("friend_vanish");
    public static final RegistryObject<SoundEvent> FUN_WORLD_WARP = register("fun_world_warp");
    public static final RegistryObject<SoundEvent> EYES_OF_FUN_SPOT = register("eyes_of_fun_spot");
    public static final RegistryObject<SoundEvent> EYES_OF_FUN_KILL = register("eyes_of_fun_kill");
    public static final RegistryObject<SoundEvent> SMILE_ROAR = register("smile_roar");
    public static final RegistryObject<SoundEvent> SMILE_STEP = register("smile_step");
    public static final RegistryObject<SoundEvent> MAZE_PORTAL = register("maze_portal");
    public static final RegistryObject<SoundEvent> THE_TRUTH_ROAR = register("the_truth_roar");
    public static final RegistryObject<SoundEvent> THE_TRUTH_EMERGE = register("the_truth_emerge");
    public static final RegistryObject<SoundEvent> THE_TRUTH_KILL = register("the_truth_kill");
    public static final RegistryObject<SoundEvent> THE_TRUTH_AMBIENT = register("the_truth_ambient");

    private ModSounds() {}

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(UnderWorldMod.id(name)));
    }
}
