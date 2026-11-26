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

        public final ForgeConfigSpec.BooleanValue friendEnabled;
        public final ForgeConfigSpec.IntValue friendEveningTime;
        public final ForgeConfigSpec.ConfigValue<String> friendDisplayName;
        public final ForgeConfigSpec.BooleanValue friendJoinBroadcast;
        public final ForgeConfigSpec.BooleanValue friendAmbientChatEnabled;
        public final ForgeConfigSpec.IntValue friendAmbientChatIntervalTicks;
        public final ForgeConfigSpec.BooleanValue friendChatResponsesEnabled;
        public final ForgeConfigSpec.BooleanValue friendProfanityPunishmentEnabled;

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
                    .comment("Lets friend reply to keywords like hello, who are you?, void, hands, tree, and the sky.")
                    .translation("under_world.config.friendChatResponsesEnabled")
                    .define("friendChatResponsesEnabled", true);
            this.friendProfanityPunishmentEnabled = builder
                    .comment("Triggers lightning, explosion, or high-altitude falling anvil when players insult friend.")
                    .translation("under_world.config.friendProfanityPunishmentEnabled")
                    .define("friendProfanityPunishmentEnabled", true);
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
