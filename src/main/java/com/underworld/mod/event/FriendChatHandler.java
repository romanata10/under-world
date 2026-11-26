package com.underworld.mod.event;

import com.underworld.mod.config.UnderWorldConfigHolder;
import com.underworld.mod.world.UnderWorldSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.regex.Pattern;

public final class FriendChatHandler {
    private static final Pattern EN_PROFANITY = Pattern.compile(
            "\\b(fuck(?:ing|ed|er|off)?|shit|bitch|asshole|bastard|dick|cunt|idiot|moron|stupid|dumbass|loser|kys|shut\\s+up|hate\\s+you|go\\s+away|trash)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern RU_PROFANITY = Pattern.compile(
            "(?:^|[^\\p{L}])(сук[аи]|бля(?:ть|д[ьи]|)?|нахуй|ху[йяеи]|пизд[аеуоы]|еб(?:ан|ат|ут|ёшь|ал)|ёб|дурак|идиот|тварь|ублюдок|мудак|дебил|чмо|пидор(?:ас)?|заткнись|лох|мразь|гнида|тупой)(?:[^\\p{L}]|$)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS
    );

    private static final Pattern WHO_ARE_YOU = Pattern.compile(
            "(?:^|[^\\p{L}])(who\\s+are\\s+you|кто\\s+ты)(?:[^\\p{L}]|$)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS
    );
    private static final Pattern FRIEND_WORD = Pattern.compile(
            "(?:^|[^\\p{L}])(friend|друг)(?:[^\\p{L}]|$)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS
    );
    private static final Pattern HELLO_WORD = Pattern.compile(
            "(?:^|[^\\p{L}])(hello|hi|привет)(?:[^\\p{L}]|$)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS
    );
    private static final Pattern VOID_WORD = Pattern.compile(
            "(?:^|[^\\p{L}])void(?:[^\\p{L}]|$)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern HANDS_WORD = Pattern.compile(
            "(?:^|[^\\p{L}])hands(?:[^\\p{L}]|$)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern TREE_WORD = Pattern.compile(
            "(?:^|[^\\p{L}])tree(?:[^\\p{L}]|$)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern THE_SKY_PHRASE = Pattern.compile(
            "(?:^|[^\\p{L}])the\\s+sky(?:[^\\p{L}]|$)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern FUN_WORLD_PHRASE = Pattern.compile(
            "(?:^|[^\\p{L}])(fun\\s+world|eyes\\s+of\\s+fun|вес[ёе]л(?:ый|ого)\\s+мир|глаза)(?:[^\\p{L}]|$)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS
    );
    private static final Pattern MAZE_PHRASE = Pattern.compile(
            "(?:^|[^\\p{L}])(maze|лабиринт)(?:[^\\p{L}]|$)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS
    );
    private static final Pattern SMILE_PHRASE = Pattern.compile(
            "(?:^|[^\\p{L}])(smile|улыбк[аи])(?:[^\\p{L}]|$)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS
    );

    private FriendChatHandler() {}

    @SubscribeEvent
    public static void onServerChat(final ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        if (UnderWorldConfigHolder.get() != null
                && !UnderWorldConfigHolder.get().friend.friendChatResponsesEnabled.get()) {
            return;
        }

        UnderWorldSavedData data = UnderWorldSavedData.get(server);
        if (!data.hasFriendJoined() && !UnderWorldEventDirector.hasFriendInServer(server, data)) {
            return;
        }

        String rawText = event.getRawText();
        if (rawText == null || rawText.isBlank()) {
            return;
        }

        if (isProfaneOrInsult(rawText)) {
            UnderWorldEventDirector.schedule(18, () -> UnderWorldEventDirector.broadcastFriendChat(server, "why.."));
            boolean punishEnabled = UnderWorldConfigHolder.get() == null
                    || UnderWorldConfigHolder.get().friend.friendProfanityPunishmentEnabled.get();
            if (punishEnabled) {
                UnderWorldEventDirector.schedule(34, () -> {
                    ServerPlayer online = server.getPlayerList().getPlayer(player.getUUID());
                    if (online != null && online.isAlive()) {
                        punishPlayer(online);
                    }
                });
            }
            return;
        }

        String reply = resolveReply(rawText);
        if (reply != null) {
            int delay = 18 + server.overworld().getRandom().nextInt(18);
            UnderWorldEventDirector.schedule(delay, () -> UnderWorldEventDirector.broadcastFriendChat(server, reply));
        }
    }

    public static boolean isProfaneOrInsult(String text) {
        String lower = text.toLowerCase(Locale.ROOT).trim();
        return EN_PROFANITY.matcher(lower).find() || RU_PROFANITY.matcher(lower).find();
    }

    @Nullable
    public static String resolveReply(String text) {
        String lower = text.toLowerCase(Locale.ROOT).trim();
        if (WHO_ARE_YOU.matcher(lower).find()) {
            return "your REAL friend";
        }
        if (FUN_WORLD_PHRASE.matcher(lower).find()) {
            return "so bright. dont blink at night.";
        }
        if (MAZE_PHRASE.matcher(lower).find()) {
            return "red bricks. keep running.";
        }
        if (SMILE_PHRASE.matcher(lower).find()) {
            return "he is smiling behind you";
        }
        if (FRIEND_WORD.matcher(lower).find()) {
            return "yes";
        }
        if (HELLO_WORD.matcher(lower).find()) {
            return "hello";
        }
        if (VOID_WORD.matcher(lower).find()) {
            return "under";
        }
        if (HANDS_WORD.matcher(lower).find()) {
            return "in cave";
        }
        if (TREE_WORD.matcher(lower).find()) {
            return "alive";
        }
        if (THE_SKY_PHRASE.matcher(lower).find()) {
            return "is here";
        }
        return null;
    }

    private static void punishPlayer(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        int mode = level.getRandom().nextInt(3);

        switch (mode) {
            case 0 -> {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                if (bolt != null) {
                    bolt.moveTo(player.getX(), player.getY(), player.getZ());
                    bolt.setVisualOnly(false);
                    level.addFreshEntity(bolt);
                }
                player.hurt(level.damageSources().lightningBolt(), 1000.0F);
            }
            case 1 -> {
                level.explode(null, player.getX(), player.getY(), player.getZ(), 4.2F, Level.ExplosionInteraction.TNT);
                if (player.isAlive()) {
                    player.hurt(level.damageSources().explosion(null), 100.0F);
                }
            }
            default -> {
                BlockPos targetPos = player.blockPosition();
                int dropOffset = Math.min(26, Math.max(8, level.getMaxBuildHeight() - targetPos.getY() - 2));
                BlockPos anvilPos = targetPos.above(dropOffset);

                for (int dy = 2; dy <= dropOffset; dy++) {
                    BlockPos check = targetPos.above(dy);
                    BlockState state = level.getBlockState(check);
                    if (!state.isAir() && state.getDestroySpeed(level, check) >= 0.0F && level.getBlockEntity(check) == null) {
                        level.destroyBlock(check, false);
                    }
                }

                FallingBlockEntity anvil = FallingBlockEntity.fall(level, anvilPos, Blocks.ANVIL.defaultBlockState());
                anvil.setHurtsEntities(14.0F, 150);
                anvil.disableDrop();
            }
        }
    }
}
