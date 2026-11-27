package com.underworld.mod.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class UnderWorldForgeConfig {
    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static {
        Pair<Common, ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = commonPair.getLeft();
        COMMON_SPEC = commonPair.getRight();

        Pair<Client, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = clientPair.getLeft();
        CLIENT_SPEC = clientPair.getRight();
    }

    private UnderWorldForgeConfig() {}

    public static final class Common {
        public final ForgeConfigSpec.BooleanValue modEnabled;
        public final ForgeConfigSpec.IntValue spawnMinDistance;
        public final ForgeConfigSpec.IntValue spawnMaxDistance;
        public final ForgeConfigSpec.BooleanValue debugLogging;
        public final ForgeConfigSpec.BooleanValue antiSpawnKillEnabled;
        public final ForgeConfigSpec.BooleanValue dynamicStructureEventsEnabled;
        public final ForgeConfigSpec.IntValue dynamicStructureIntervalTicks;

        public final ForgeConfigSpec.BooleanValue friendEnabled;
        public final ForgeConfigSpec.IntValue friendEveningTime;
        public final ForgeConfigSpec.ConfigValue<String> friendDisplayName;
        public final ForgeConfigSpec.BooleanValue friendJoinBroadcast;
        public final ForgeConfigSpec.BooleanValue friendAmbientChatEnabled;
        public final ForgeConfigSpec.IntValue friendAmbientChatIntervalTicks;
        public final ForgeConfigSpec.BooleanValue friendChatResponsesEnabled;
        public final ForgeConfigSpec.BooleanValue friendProfanityPunishmentEnabled;
        public final ForgeConfigSpec.BooleanValue friendStalkerEnabled;
        public final ForgeConfigSpec.DoubleValue friendFunWorldWarpChance;
        public final ForgeConfigSpec.BooleanValue friendDoorInteractionEnabled;

        public final ForgeConfigSpec.BooleanValue handsEnabled;
        public final ForgeConfigSpec.DoubleValue handsCaveSpawnChance;
        public final ForgeConfigSpec.IntValue handsChaseDurationTicks;
        public final ForgeConfigSpec.BooleanValue handsBreakBlocks;
        public final ForgeConfigSpec.BooleanValue handsKickOnKill;

        public final ForgeConfigSpec.BooleanValue treeMimicEnabled;
        public final ForgeConfigSpec.DoubleValue treeMimicSpawnChance;
        public final ForgeConfigSpec.DoubleValue treeMimicTriggerRange;
        public final ForgeConfigSpec.BooleanValue treeMimicRequireAxe;

        public final ForgeConfigSpec.BooleanValue theSkyEnabled;
        public final ForgeConfigSpec.DoubleValue theSkySpawnChance;
        public final ForgeConfigSpec.DoubleValue theSkyVanishRange;
        public final ForgeConfigSpec.DoubleValue theSkyStompChance;

        public final ForgeConfigSpec.BooleanValue theTruthEnabled;
        public final ForgeConfigSpec.DoubleValue theTruthSpawnChance;
        public final ForgeConfigSpec.IntValue theTruthChaseDurationTicks;
        public final ForgeConfigSpec.DoubleValue theTruthChaseSpeed;
        public final ForgeConfigSpec.BooleanValue theTruthBreakBlocks;
        public final ForgeConfigSpec.BooleanValue theTruthKickOnKill;
        public final ForgeConfigSpec.BooleanValue theTruthSpawnPillarOnRejoin;

        public final ForgeConfigSpec.BooleanValue clockEnabled;
        public final ForgeConfigSpec.DoubleValue clockSpawnChance;
        public final ForgeConfigSpec.DoubleValue clockTimeSpeed;

        public final ForgeConfigSpec.BooleanValue theMimicEnabled;
        public final ForgeConfigSpec.DoubleValue theMimicSpawnChance;
        public final ForgeConfigSpec.DoubleValue theMimicTriggerRange;
        public final ForgeConfigSpec.IntValue theMimicChaseDurationTicks;

        public final ForgeConfigSpec.BooleanValue demonFishEnabled;
        public final ForgeConfigSpec.DoubleValue demonFishSpawnChance;
        public final ForgeConfigSpec.IntValue demonFishChaseDurationTicks;
        public final ForgeConfigSpec.BooleanValue demonFishBreakBlocks;

        public final ForgeConfigSpec.BooleanValue theBallEnabled;
        public final ForgeConfigSpec.DoubleValue theBallSpawnChance;
        public final ForgeConfigSpec.IntValue theBallEffectDurationTicks;

        public final ForgeConfigSpec.BooleanValue funWorldEnabled;
        public final ForgeConfigSpec.IntValue funWorldTimeSpeedMultiplier;
        public final ForgeConfigSpec.BooleanValue eyesOfFunEnabled;
        public final ForgeConfigSpec.BooleanValue eyesOfFunInstantKill;
        public final ForgeConfigSpec.BooleanValue mazeEnabled;
        public final ForgeConfigSpec.DoubleValue smileSprintSpeed;
        public final ForgeConfigSpec.BooleanValue errorDimensionEnabled;
        public final ForgeConfigSpec.DoubleValue errorBedWarpChance;
        public final ForgeConfigSpec.IntValue errorStayDurationTicks;
        public final ForgeConfigSpec.BooleanValue timeDimensionEnabled;
        public final ForgeConfigSpec.BooleanValue clockTimeHuntEnabled;
        public final ForgeConfigSpec.BooleanValue customMusicEnabled;
        public final ForgeConfigSpec.BooleanValue customOverworldMusic;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("general");
            this.modEnabled = builder
                    .comment("Master switch for automatic Under World horror events.")
                    .translation("under_world.config.modEnabled")
                    .define("modEnabled", true);
            this.spawnMinDistance = builder
                    .comment("Minimum offset distance in blocks used by /underworld spawn.")
                    .translation("under_world.config.spawnMinDistance")
                    .defineInRange("spawnMinDistance", 15, 5, 48);
            this.spawnMaxDistance = builder
                    .comment("Maximum offset distance in blocks used by /underworld spawn.")
                    .translation("under_world.config.spawnMaxDistance")
                    .defineInRange("spawnMaxDistance", 20, 10, 64);
            this.debugLogging = builder
                    .comment("Logs detailed event scheduling and entity state transitions.")
                    .translation("under_world.config.debugLogging")
                    .define("debugLogging", false);
            this.antiSpawnKillEnabled = builder
                    .comment("Removes Under World entities that kill a player so they cannot spawn-kill on respawn.")
                    .translation("under_world.config.antiSpawnKillEnabled")
                    .define("antiSpawnKillEnabled", true);
            this.dynamicStructureEventsEnabled = builder
                    .comment("Spawns oddity and lore structures dynamically as live survival events around players.")
                    .define("dynamicStructureEventsEnabled", true);
            this.dynamicStructureIntervalTicks = builder
                    .comment("Average ticks between dynamic survival structure events (4800 = 4 minutes).")
                    .defineInRange("dynamicStructureIntervalTicks", 4800, 600, 24000);
            builder.pop();

            builder.push("friend");
            this.friendEnabled = builder
                    .comment("Enables the one-time Day 1 evening join event for friend.")
                    .translation("under_world.config.friendEnabled")
                    .define("friendEnabled", true);
            this.friendEveningTime = builder
                    .comment("Daytime tick (0-23999) on Day 1 when friend joins (default 12500 = evening).")
                    .translation("under_world.config.friendEveningTime")
                    .defineInRange("friendEveningTime", 12500, 1000, 23000);
            this.friendDisplayName = builder
                    .comment("Username shown in join message, chat, and player tab list.")
                    .translation("under_world.config.friendDisplayName")
                    .define("friendDisplayName", "friend");
            this.friendJoinBroadcast = builder
                    .comment("Broadcasts the server join message and plays a cave sound when friend arrives.")
                    .translation("under_world.config.friendJoinBroadcast")
                    .define("friendJoinBroadcast", true);
            this.friendAmbientChatEnabled = builder
                    .comment("Periodically sends eerie messages from friend after it has joined.")
                    .translation("under_world.config.friendAmbientChatEnabled")
                    .define("friendAmbientChatEnabled", true);
            this.friendAmbientChatIntervalTicks = builder
                    .comment("Ticks between ambient friend chat checks.")
                    .translation("under_world.config.friendAmbientChatIntervalTicks")
                    .defineInRange("friendAmbientChatIntervalTicks", 6000, 600, 36000);
            this.friendChatResponsesEnabled = builder
                    .comment("Lets friend reply to keywords.")
                    .translation("under_world.config.friendChatResponsesEnabled")
                    .define("friendChatResponsesEnabled", true);
            this.friendProfanityPunishmentEnabled = builder
                    .comment("Triggers lightning, explosion, or high-altitude falling anvil when players insult friend.")
                    .translation("under_world.config.friendProfanityPunishmentEnabled")
                    .define("friendProfanityPunishmentEnabled", true);
            this.friendStalkerEnabled = builder
                    .comment("Enables physical stalking AI for friend.")
                    .translation("under_world.config.friendStalkerEnabled")
                    .define("friendStalkerEnabled", true);
            this.friendFunWorldWarpChance = builder
                    .comment("Chance when spotted closely that friend pulls the player to The Fun World.")
                    .translation("under_world.config.friendFunWorldWarpChance")
                    .defineInRange("friendFunWorldWarpChance", 0.28D, 0.0D, 1.0D);
            this.friendDoorInteractionEnabled = builder
                    .comment("Allows friend to open nearby doors and leave cryptic signs.")
                    .translation("under_world.config.friendDoorInteractionEnabled")
                    .define("friendDoorInteractionEnabled", true);
            builder.pop();

            builder.push("hands");
            this.handsEnabled = builder
                    .comment("Allows rare cave encounters with Hands.")
                    .translation("under_world.config.handsEnabled")
                    .define("handsEnabled", true);
            this.handsCaveSpawnChance = builder
                    .comment("Chance per check while a player is deep underground in darkness.")
                    .translation("under_world.config.handsCaveSpawnChance")
                    .defineInRange("handsCaveSpawnChance", 0.08D, 0.0D, 1.0D);
            this.handsChaseDurationTicks = builder
                    .comment("How long Hands chases before burrowing underground (1200 ticks = 1 minute).")
                    .translation("under_world.config.handsChaseDurationTicks")
                    .defineInRange("handsChaseDurationTicks", 1200, 200, 6000);
            this.handsBreakBlocks = builder
                    .comment("Allows Hands to shatter breakable blocks around and behind itself during pursuit.")
                    .translation("under_world.config.handsBreakBlocks")
                    .define("handsBreakBlocks", true);
            this.handsKickOnKill = builder
                    .comment("Kicks the killed player from the world/server after Hands slays them.")
                    .translation("under_world.config.handsKickOnKill")
                    .define("handsKickOnKill", true);
            builder.pop();

            builder.push("tree_mimic");
            this.treeMimicEnabled = builder
                    .comment("Allows rare forested encounters with Tree?.")
                    .translation("under_world.config.treeMimicEnabled")
                    .define("treeMimicEnabled", true);
            this.treeMimicSpawnChance = builder
                    .comment("Chance per check when players explore wooded biomes.")
                    .translation("under_world.config.treeMimicSpawnChance")
                    .defineInRange("treeMimicSpawnChance", 0.07D, 0.0D, 1.0D);
            this.treeMimicTriggerRange = builder
                    .comment("Distance in blocks at which Tree? awakens its eyes and tentacles.")
                    .translation("under_world.config.treeMimicTriggerRange")
                    .defineInRange("treeMimicTriggerRange", 8.5D, 2.0D, 24.0D);
            this.treeMimicRequireAxe = builder
                    .comment("Requires an axe for the finishing blow on Tree?.")
                    .translation("under_world.config.treeMimicRequireAxe")
                    .define("treeMimicRequireAxe", true);
            builder.pop();

            builder.push("the_sky");
            this.theSkyEnabled = builder
                    .comment("Allows super-rare distant sightings of The Sky.")
                    .translation("under_world.config.theSkyEnabled")
                    .define("theSkyEnabled", true);
            this.theSkySpawnChance = builder
                    .comment("Chance per event check on the surface.")
                    .translation("under_world.config.theSkySpawnChance")
                    .defineInRange("theSkySpawnChance", 0.015D, 0.0D, 1.0D);
            this.theSkyVanishRange = builder
                    .comment("Distance in blocks at which approaching The Sky causes it to stomp or vanish.")
                    .translation("under_world.config.theSkyVanishRange")
                    .defineInRange("theSkyVanishRange", 24.0D, 8.0D, 64.0D);
            this.theSkyStompChance = builder
                    .comment("Probability that The Sky stomps the ground before vanishing when approached.")
                    .translation("under_world.config.theSkyStompChance")
                    .defineInRange("theSkyStompChance", 0.25D, 0.0D, 1.0D);
            builder.pop();

            builder.push("the_truth");
            this.theTruthEnabled = builder
                    .comment("Enables the super-mega-rare emergence event of The Truth.")
                    .translation("under_world.config.theTruthEnabled")
                    .define("theTruthEnabled", true);
            this.theTruthSpawnChance = builder
                    .comment("Super-mega-rare chance per check for The Truth to emerge ahead of a player.")
                    .translation("under_world.config.theTruthSpawnChance")
                    .defineInRange("theTruthSpawnChance", 0.0025D, 0.0D, 1.0D);
            this.theTruthChaseDurationTicks = builder
                    .comment("Duration of The Truth pursuit in ticks before safely clearing (600 = 30 seconds).")
                    .translation("under_world.config.theTruthChaseDurationTicks")
                    .defineInRange("theTruthChaseDurationTicks", 600, 100, 2400);
            this.theTruthChaseSpeed = builder
                    .comment("Chase movement speed of The Truth.")
                    .translation("under_world.config.theTruthChaseSpeed")
                    .defineInRange("theTruthChaseSpeed", 0.255D, 0.10D, 0.55D);
            this.theTruthBreakBlocks = builder
                    .comment("Allows The Truth to break any destructible blocks in its 30x50 path.")
                    .translation("under_world.config.theTruthBreakBlocks")
                    .define("theTruthBreakBlocks", true);
            this.theTruthKickOnKill = builder
                    .comment("Kicks the victim from the world/server if The Truth catches them before the timer ends.")
                    .translation("under_world.config.theTruthKickOnKill")
                    .define("theTruthKickOnKill", true);
            this.theTruthSpawnPillarOnRejoin = builder
                    .comment("Spawns a 100-block tall cobblestone pillar at the victim's spot upon rejoin.")
                    .translation("under_world.config.theTruthSpawnPillarOnRejoin")
                    .define("theTruthSpawnPillarOnRejoin", true);
            builder.pop();

            builder.push("clock");
            this.clockEnabled = builder
                    .comment("Enables rare Overworld encounters with Clock.")
                    .define("clockEnabled", true);
            this.clockSpawnChance = builder
                    .comment("Chance per Overworld surface check for Clock to appear.")
                    .defineInRange("clockSpawnChance", 0.055D, 0.0D, 1.0D);
            this.clockTimeSpeed = builder
                    .comment("Movement speed of the aggressive ticking Clock variant in The Time.")
                    .defineInRange("clockTimeSpeed", 0.42D, 0.20D, 0.70D);
            builder.pop();

            builder.push("the_mimic");
            this.theMimicEnabled = builder
                    .comment("Enables encounters with the mimic disguised as Steve or previous victims.")
                    .define("theMimicEnabled", true);
            this.theMimicSpawnChance = builder
                    .comment("Chance per Overworld surface check for the mimic to appear.")
                    .defineInRange("theMimicSpawnChance", 0.065D, 0.0D, 1.0D);
            this.theMimicTriggerRange = builder
                    .comment("Approach distance in blocks that bursts the disguise into black pixels and unleashes the mimic.")
                    .defineInRange("theMimicTriggerRange", 9.5D, 3.0D, 20.0D);
            this.theMimicChaseDurationTicks = builder
                    .comment("Duration in ticks that the mimic chases before vanishing if it catches no one (600 = 30s).")
                    .defineInRange("theMimicChaseDurationTicks", 600, 200, 2400);
            builder.pop();

            builder.push("demon_fish");
            this.demonFishEnabled = builder
                    .comment("Enables aquatic Demon Fish hunts when players swim or sail in water.")
                    .define("demonFishEnabled", true);
            this.demonFishSpawnChance = builder
                    .comment("Chance per aquatic check for Demon Fish to emerge in water.")
                    .defineInRange("demonFishSpawnChance", 0.095D, 0.0D, 1.0D);
            this.demonFishChaseDurationTicks = builder
                    .comment("Max chase time in ticks without catching the player before Demon Fish vanishes (1200 = 1 minute).")
                    .defineInRange("demonFishChaseDurationTicks", 1200, 300, 3600);
            this.demonFishBreakBlocks = builder
                    .comment("Allows Demon Fish to shatter underwater dug blocks when pursuing submerged players.")
                    .define("demonFishBreakBlocks", true);
            builder.pop();

            builder.push("the_ball");
            this.theBallEnabled = builder
                    .comment("Enables floating morphing black spheres (The Ball) around Overworld players.")
                    .define("theBallEnabled", true);
            this.theBallSpawnChance = builder
                    .comment("Chance per Overworld surface check for The Ball to appear.")
                    .defineInRange("theBallSpawnChance", 0.06D, 0.0D, 1.0D);
            this.theBallEffectDurationTicks = builder
                    .comment("Duration in ticks of the Geometric Nightmare effect applied upon touching The Ball (400 = 20s).")
                    .defineInRange("theBallEffectDurationTicks", 400, 100, 2400);
            builder.pop();

            builder.push("dimensions");
            this.funWorldEnabled = builder
                    .comment("Allows warping players into The Fun World.")
                    .define("funWorldEnabled", true);
            this.funWorldTimeSpeedMultiplier = builder
                    .comment("Day/night speed multiplier in The Fun World (4 = 4x faster than Overworld).")
                    .defineInRange("funWorldTimeSpeedMultiplier", 4, 1, 12);
            this.eyesOfFunEnabled = builder
                    .comment("Spawns Eyes of Fun during nightfall in The Fun World.")
                    .define("eyesOfFunEnabled", true);
            this.eyesOfFunInstantKill = builder
                    .comment("When true, Eyes of Fun kills caught players in one lethal strike.")
                    .define("eyesOfFunInstantKill", true);
            this.mazeEnabled = builder
                    .comment("Enables Maze rift portals and Smile inside The Maze dimension.")
                    .define("mazeEnabled", true);
            this.smileSprintSpeed = builder
                    .comment("Base sprint speed of Smile in The Maze.")
                    .defineInRange("smileSprintSpeed", 0.43D, 0.25D, 0.70D);
            this.errorDimensionEnabled = builder
                    .comment("Allows sleeping in an Overworld bed to randomly warp players to The Error.")
                    .define("errorDimensionEnabled", true);
            this.errorBedWarpChance = builder
                    .comment("Probability upon sleeping in a bed to be pulled into The Error (0.20 = 20%).")
                    .defineInRange("errorBedWarpChance", 0.20D, 0.0D, 1.0D);
            this.errorStayDurationTicks = builder
                    .comment("Ticks spent in The Error before auto-returning to Overworld (1800 = 1m 30s).")
                    .defineInRange("errorStayDurationTicks", 1800, 200, 12000);
            this.timeDimensionEnabled = builder
                    .comment("Enables The Time dimension and warping caught players there via Clock.")
                    .define("timeDimensionEnabled", true);
            this.clockTimeHuntEnabled = builder
                    .comment("Spawns the aggressive ticking Clock variant inside The Time dimension.")
                    .define("clockTimeHuntEnabled", true);
            this.customMusicEnabled = builder
                    .comment("Enables custom dimension music across Under World dimensions.")
                    .define("customMusicEnabled", true);
            this.customOverworldMusic = builder
                    .comment("Plays Under World eerie ambient music in the Overworld as well.")
                    .define("customOverworldMusic", true);
            builder.pop();
        }
    }

    public static final class Client {
        public final ForgeConfigSpec.BooleanValue screenAtmosphereEffects;

        public Client(ForgeConfigSpec.Builder builder) {
            builder.push("client");
            this.screenAtmosphereEffects = builder
                    .comment("Enables subtle client audio/atmosphere emphasis during nearby horror encounters.")
                    .translation("under_world.config.screenAtmosphereEffects")
                    .define("screenAtmosphereEffects", true);
            builder.pop();
        }
    }
}
