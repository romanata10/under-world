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

    public static final RegistryObject<SoundEvent> CLOCK_TICK = register("clock_tick");
    public static final RegistryObject<SoundEvent> CLOCK_CHIME = register("clock_chime");
    public static final RegistryObject<SoundEvent> ERROR_WARP = register("error_warp");
    public static final RegistryObject<SoundEvent> TIME_WARP = register("time_warp");

    public static final RegistryObject<SoundEvent> MIMIC_BURST = register("mimic_burst");
    public static final RegistryObject<SoundEvent> MIMIC_CHASE = register("mimic_chase");
    public static final RegistryObject<SoundEvent> DEMON_FISH_AMBUSH = register("demon_fish_ambush");
    public static final RegistryObject<SoundEvent> DEMON_FISH_TONGUE = register("demon_fish_tongue");
    public static final RegistryObject<SoundEvent> THE_BALL_HUM = register("the_ball_hum");
    public static final RegistryObject<SoundEvent> GEOMETRIC_NIGHTMARE = register("geometric_nightmare");

    public static final RegistryObject<SoundEvent> PYLON_STEP = register("pylon_step");
    public static final RegistryObject<SoundEvent> PYLON_HUM = register("pylon_hum");
    public static final RegistryObject<SoundEvent> PYLON_ELECTROCUTE = register("pylon_electrocute");
    public static final RegistryObject<SoundEvent> TURBINE_STEP = register("turbine_step");
    public static final RegistryObject<SoundEvent> TURBINE_WHOOSH = register("turbine_whoosh");
    public static final RegistryObject<SoundEvent> XP_STARTUP = register("xp_startup");
    public static final RegistryObject<SoundEvent> XP_ERROR = register("xp_error");
    public static final RegistryObject<SoundEvent> DOT_COM_ROAR = register("dot_com_roar");
    public static final RegistryObject<SoundEvent> HELL_EVENT = register("hell_event");

    public static final RegistryObject<SoundEvent> MUSIC_FUN_WORLD = register("music_fun_world");
    public static final RegistryObject<SoundEvent> MUSIC_MAZE = register("music_maze");
    public static final RegistryObject<SoundEvent> MUSIC_THE_ERROR = register("music_the_error");
    public static final RegistryObject<SoundEvent> MUSIC_THE_TIME = register("music_the_time");
    public static final RegistryObject<SoundEvent> MUSIC_PLAINS = register("music_plains");
    public static final RegistryObject<SoundEvent> MUSIC_WIND_OCEAN = register("music_wind_ocean");
    public static final RegistryObject<SoundEvent> MUSIC_XP = register("music_xp");
    public static final RegistryObject<SoundEvent> MUSIC_BROWSER = register("music_browser");
    public static final RegistryObject<SoundEvent> MUSIC_OVERWORLD = register("music_overworld");

    private ModSounds() {}

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(UnderWorldMod.id(name)));
    }
}
