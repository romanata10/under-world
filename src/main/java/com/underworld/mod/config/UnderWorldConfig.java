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

@TomlHeaderComment(text = "Under World Horror Mod Configuration (synchronized in multiplayer)")
@Version(version = 1)
public class UnderWorldConfig extends Config {

    public GeneralSection general = new GeneralSection();
    public FriendSection friend = new FriendSection();
    public HandsSection hands = new HandsSection();
    public TreeMimicSection treeMimic = new TreeMimicSection();
    public TheSkySection theSky = new TheSkySection();

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
}
