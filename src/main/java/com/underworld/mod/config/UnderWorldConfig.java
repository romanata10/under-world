package com.underworld.mod.config;

import com.underworld.mod.UnderWorldMod;
import me.fzzyhmstrs.fzzy_config.annotations.Comment;
import me.fzzyhmstrs.fzzy_config.annotations.TomlHeaderComment;
import me.fzzyhmstrs.fzzy_config.annotations.Version;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedString;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;

@TomlHeaderComment(text = "Under World Horror Mod Configuration v2 (synchronized in multiplayer)")
@Version(version = 2)
public class UnderWorldConfig extends Config {

    public GeneralSection general = new GeneralSection();
    public FriendSection friend = new FriendSection();
    public HandsSection hands = new HandsSection();
    public TreeMimicSection treeMimic = new TreeMimicSection();
    public TheSkySection theSky = new TheSkySection();
    public TheTruthSection theTruth = new TheTruthSection();
    public ClockSection clock = new ClockSection();
    public TheMimicSection theMimic = new TheMimicSection();
    public DemonFishSection demonFish = new DemonFishSection();
    public TheBallSection theBall = new TheBallSection();
    public DimensionsSection dimensions = new DimensionsSection();

    public UnderWorldConfig() {
        super(UnderWorldMod.id("settings"));
    }

    @Override
    public void onUpdateClient() {
        super.onUpdateClient();
        UnderWorldConfigHolder.syncFzzyToForge();
    }

    public static class GeneralSection extends ConfigSection {
        @Comment("Master switch for automatic Under World horror events.")
        public ValidatedBoolean modEnabled = new ValidatedBoolean(true);

        @Comment("Minimum distance in blocks used by /underworld spawn.")
        public ValidatedInt spawnMinDistance = new ValidatedInt(15, 48, 5);

        @Comment("Maximum distance in blocks used by /underworld spawn.")
        public ValidatedInt spawnMaxDistance = new ValidatedInt(20, 64, 10);

        @Comment("Enable verbose diagnostic logging for event scheduling.")
        public ValidatedBoolean debugLogging = new ValidatedBoolean(false);

        @Comment("Removes any Under World entity that kills a player (and nearby aggro entities) so it cannot spawn-kill on respawn.")
        public ValidatedBoolean antiSpawnKillEnabled = new ValidatedBoolean(true);

        @Comment("Spawns oddity and server-lore structures dynamically as survival events around players.")
        public ValidatedBoolean dynamicStructureEventsEnabled = new ValidatedBoolean(true);

        @Comment("Average ticks between dynamic survival structure events (4800 = 4 minutes).")
        public ValidatedInt dynamicStructureIntervalTicks = new ValidatedInt(4800, 24000, 600);
    }

    public static class FriendSection extends ConfigSection {
        @Comment("Enables the one-time Day 1 evening join event for friend.")
        public ValidatedBoolean friendEnabled = new ValidatedBoolean(true);

        @Comment("Daytime tick (0-23999) on Day 1 when friend joins (default 12500 = evening).")
        public ValidatedInt friendEveningTime = new ValidatedInt(12500, 23000, 1000);

        @Comment("Display name shown in chat, join broadcast, and the multiplayer tab list.")
        public ValidatedString friendDisplayName = new ValidatedString("friend");

        @Comment("Broadcasts a yellow server join message and cave sound when friend enters the world.")
        public ValidatedBoolean friendJoinBroadcast = new ValidatedBoolean(true);

        @Comment("Enables ambient chat whispers from friend after it has joined.")
        public ValidatedBoolean friendAmbientChatEnabled = new ValidatedBoolean(true);

        @Comment("Interval in ticks between ambient friend chat checks.")
        public ValidatedInt friendAmbientChatIntervalTicks = new ValidatedInt(6000, 36000, 600);

        @Comment("Allows friend to reply to player chat triggers in English and Russian.")
        public ValidatedBoolean friendChatResponsesEnabled = new ValidatedBoolean(true);

        @Comment("Punishes players who swear at or insult friend (lightning, explosion, or high falling anvil).")
        public ValidatedBoolean friendProfanityPunishmentEnabled = new ValidatedBoolean(true);

        @Comment("Spawns friend physically near players to stalk, peek around doors/trees, leave Void Shrines, and vanish.")
        public ValidatedBoolean friendStalkerEnabled = new ValidatedBoolean(true);

        @Comment("Chance when spotted up close that friend warps the player into The Fun World instead of only vanishing.")
        public ValidatedDouble friendFunWorldWarpChance = new ValidatedDouble(0.28D, 1.0D, 0.0D);

        @Comment("Allows friend to open nearby doors and leave cryptic wooden signs.")
        public ValidatedBoolean friendDoorInteractionEnabled = new ValidatedBoolean(true);
    }

    public static class HandsSection extends ConfigSection {
        @Comment("Enables rare deep-cave encounters with Hands.")
        public ValidatedBoolean handsEnabled = new ValidatedBoolean(true);

        @Comment("Chance per event check when a player is underground in darkness.")
        public ValidatedDouble handsCaveSpawnChance = new ValidatedDouble(0.08D, 1.0D, 0.0D);

        @Comment("Chase duration in ticks before Hands burrows underground (1200 = 1 minute).")
        public ValidatedInt handsChaseDurationTicks = new ValidatedInt(1200, 6000, 200);

        @Comment("Allows Hands to break surrounding blocks while chasing its target.")
        public ValidatedBoolean handsBreakBlocks = new ValidatedBoolean(true);

        @Comment("Disconnects/kicks the victim when Hands kills them.")
        public ValidatedBoolean handsKickOnKill = new ValidatedBoolean(true);
    }

    public static class TreeMimicSection extends ConfigSection {
        @Comment("Enables rare surface forest encounters with Tree?.")
        public ValidatedBoolean treeMimicEnabled = new ValidatedBoolean(true);

        @Comment("Chance per surface event check in wooded biomes.")
        public ValidatedDouble treeMimicSpawnChance = new ValidatedDouble(0.07D, 1.0D, 0.0D);

        @Comment("Approach radius in blocks that awakens a disguised Tree?.")
        public ValidatedDouble treeMimicTriggerRange = new ValidatedDouble(8.5D, 24.0D, 2.0D);

        @Comment("Requires an axe for the finishing blow on Tree?.")
        public ValidatedBoolean treeMimicRequireAxe = new ValidatedBoolean(true);
    }

    public static class TheSkySection extends ConfigSection {
        @Comment("Enables super-rare distant encounters with The Sky.")
        public ValidatedBoolean theSkyEnabled = new ValidatedBoolean(true);

        @Comment("Chance per surface event check for The Sky to manifest far away.")
        public ValidatedDouble theSkySpawnChance = new ValidatedDouble(0.015D, 1.0D, 0.0D);

        @Comment("Distance in blocks at which approaching The Sky causes it to stomp or vanish.")
        public ValidatedDouble theSkyVanishRange = new ValidatedDouble(24.0D, 64.0D, 8.0D);

        @Comment("Probability that approaching The Sky triggers a stomp attack before vanishing.")
        public ValidatedDouble theSkyStompChance = new ValidatedDouble(0.25D, 1.0D, 0.0D);
    }

    public static class TheTruthSection extends ConfigSection {
        @Comment("Enables the super-mega-rare emergence event of The Truth.")
        public ValidatedBoolean theTruthEnabled = new ValidatedBoolean(true);

        @Comment("Super-mega-rare chance per surface/overworld check for The Truth to emerge ahead of a player.")
        public ValidatedDouble theTruthSpawnChance = new ValidatedDouble(0.0025D, 1.0D, 0.0D);

        @Comment("Duration of The Truth pursuit in ticks before the event safely clears (600 = 30 seconds).")
        public ValidatedInt theTruthChaseDurationTicks = new ValidatedInt(600, 2400, 100);

        @Comment("Relentless chase movement speed of The Truth.")
        public ValidatedDouble theTruthChaseSpeed = new ValidatedDouble(0.255D, 0.55D, 0.10D);

        @Comment("Allows The Truth to break any destructible blocks in its 30x50 path while chasing.")
        public ValidatedBoolean theTruthBreakBlocks = new ValidatedBoolean(true);

        @Comment("Kicks the victim from the world/server if The Truth catches them before the timer ends.")
        public ValidatedBoolean theTruthKickOnKill = new ValidatedBoolean(true);

        @Comment("Spawns a 100-block tall cobblestone pillar beneath the victim when they rejoin after being caught.")
        public ValidatedBoolean theTruthSpawnPillarOnRejoin = new ValidatedBoolean(true);
    }

    public static class ClockSection extends ConfigSection {
        @Comment("Enables rare Overworld encounters with Clock.")
        public ValidatedBoolean clockEnabled = new ValidatedBoolean(true);

        @Comment("Chance per Overworld surface event check for Clock to manifest.")
        public ValidatedDouble clockSpawnChance = new ValidatedDouble(0.055D, 1.0D, 0.0D);

        @Comment("Movement speed of the aggressive ticking Clock variant inside The Time.")
        public ValidatedDouble clockTimeSpeed = new ValidatedDouble(0.42D, 0.70D, 0.20D);
    }

    public static class TheMimicSection extends ConfigSection {
        @Comment("Enables encounters with the mimic disguised as Steve or previous victims.")
        public ValidatedBoolean theMimicEnabled = new ValidatedBoolean(true);

        @Comment("Chance per Overworld surface check for the mimic to appear.")
        public ValidatedDouble theMimicSpawnChance = new ValidatedDouble(0.065D, 1.0D, 0.0D);

        @Comment("Approach distance in blocks that bursts the disguise into black pixels and unleashes the mimic.")
        public ValidatedDouble theMimicTriggerRange = new ValidatedDouble(9.5D, 20.0D, 3.0D);

        @Comment("Duration in ticks that the mimic chases before vanishing if it catches no one (600 = 30s).")
        public ValidatedInt theMimicChaseDurationTicks = new ValidatedInt(600, 2400, 200);
    }

    public static class DemonFishSection extends ConfigSection {
        @Comment("Enables aquatic Demon Fish hunts when players swim or sail in water.")
        public ValidatedBoolean demonFishEnabled = new ValidatedBoolean(true);

        @Comment("Chance per aquatic check for Demon Fish to emerge in water.")
        public ValidatedDouble demonFishSpawnChance = new ValidatedDouble(0.095D, 1.0D, 0.0D);

        @Comment("Max chase time in ticks without catching the player before Demon Fish vanishes (1200 = 1 minute).")
        public ValidatedInt demonFishChaseDurationTicks = new ValidatedInt(1200, 3600, 300);

        @Comment("Allows Demon Fish to shatter underwater dug blocks when pursuing submerged players.")
        public ValidatedBoolean demonFishBreakBlocks = new ValidatedBoolean(true);
    }

    public static class TheBallSection extends ConfigSection {
        @Comment("Enables floating morphing black spheres (The Ball) around Overworld players.")
        public ValidatedBoolean theBallEnabled = new ValidatedBoolean(true);

        @Comment("Chance per Overworld surface check for The Ball to appear.")
        public ValidatedDouble theBallSpawnChance = new ValidatedDouble(0.06D, 1.0D, 0.0D);

        @Comment("Duration in ticks of the Geometric Nightmare effect applied upon touching The Ball (400 = 20s).")
        public ValidatedInt theBallEffectDurationTicks = new ValidatedInt(400, 2400, 100);
    }

    public static class DimensionsSection extends ConfigSection {
        @Comment("Enables warping players to The Fun World via Void Shrines or friend encounters.")
        public ValidatedBoolean funWorldEnabled = new ValidatedBoolean(true);

        @Comment("Day/night cycle speed multiplier inside The Fun World (4 = 4x faster than Overworld).")
        public ValidatedInt funWorldTimeSpeedMultiplier = new ValidatedInt(4, 12, 1);

        @Comment("Allows Eyes of Fun to manifest during nightfall inside The Fun World.")
        public ValidatedBoolean eyesOfFunEnabled = new ValidatedBoolean(true);

        @Comment("When true, Eyes of Fun kills caught victims in one lethal strike if they stop looking.")
        public ValidatedBoolean eyesOfFunInstantKill = new ValidatedBoolean(true);

        @Comment("Enables Maze rift portals and Smile inside The Maze dimension.")
        public ValidatedBoolean mazeEnabled = new ValidatedBoolean(true);

        @Comment("Base movement speed of Smile during relentless Maze corridor chases.")
        public ValidatedDouble smileSprintSpeed = new ValidatedDouble(0.43D, 0.70D, 0.25D);

        @Comment("Allows sleeping in an Overworld bed to randomly warp players to The Error dimension.")
        public ValidatedBoolean errorDimensionEnabled = new ValidatedBoolean(true);

        @Comment("Chance (0.0 - 1.0) upon sleeping in a bed to be pulled into The Error (default 0.20 = 20%).")
        public ValidatedDouble errorBedWarpChance = new ValidatedDouble(0.20D, 1.0D, 0.0D);

        @Comment("Ticks spent inside The Error before returning to the Overworld (1800 = 1m 30s).")
        public ValidatedInt errorStayDurationTicks = new ValidatedInt(1800, 12000, 200);

        @Comment("Enables The Time dimension and warping caught players there via Clock.")
        public ValidatedBoolean timeDimensionEnabled = new ValidatedBoolean(true);

        @Comment("Spawns the aggressive ticking Clock hunter inside The Time dimension.")
        public ValidatedBoolean clockTimeHuntEnabled = new ValidatedBoolean(true);

        @Comment("Enables custom dimension music across Under World dimensions.")
        public ValidatedBoolean customMusicEnabled = new ValidatedBoolean(true);

        @Comment("Plays Under World eerie ambient music in the Overworld as well as mod dimensions.")
        public ValidatedBoolean customOverworldMusic = new ValidatedBoolean(true);
    }
}
