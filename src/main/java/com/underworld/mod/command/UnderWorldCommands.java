package com.underworld.mod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.underworld.mod.config.UnderWorldConfigHolder;
import com.underworld.mod.entity.ClockEntity;
import com.underworld.mod.entity.EyesOfFunEntity;
import com.underworld.mod.entity.FriendEntity;
import com.underworld.mod.entity.HandsEntity;
import com.underworld.mod.entity.SmileEntity;
import com.underworld.mod.entity.TheSkyEntity;
import com.underworld.mod.entity.TheTruthEntity;
import com.underworld.mod.entity.TreeMimicEntity;
import com.underworld.mod.event.UnderWorldEventDirector;
import com.underworld.mod.network.NetworkHandler;
import com.underworld.mod.registry.ModDimensions;
import com.underworld.mod.registry.ModEntities;
import com.underworld.mod.world.UnderWorldSavedData;
import com.underworld.mod.world.structure.BrokenShaftPiece;
import com.underworld.mod.world.structure.ClockStatuePiece;
import com.underworld.mod.world.structure.ElongatedTreePiece;
import com.underworld.mod.world.structure.FountainPiece;
import com.underworld.mod.world.structure.GlitchClusterPiece;
import com.underworld.mod.world.structure.LeafMazePiece;
import com.underworld.mod.world.structure.OldVillageHousePiece;
import com.underworld.mod.world.structure.VoidShrinePiece;
import com.underworld.mod.world.structure.WheatFarmPiece;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.function.Supplier;

public final class UnderWorldCommands {
    private UnderWorldCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(final RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("underworld")
                .requires(src -> src.hasPermission(2));

        // /underworld spawn <entity> [<player>]
        LiteralArgumentBuilder<CommandSourceStack> spawnCmd = Commands.literal("spawn")
                .then(buildSpawnNode("friend", ModEntities.FRIEND, true))
                .then(buildSpawnNode("hands", ModEntities.HANDS, true))
                .then(buildSpawnNode("tree", ModEntities.TREE_MIMIC, false))
                .then(buildSpawnNode("tree_mimic", ModEntities.TREE_MIMIC, false))
                .then(buildSpawnNode("the_sky", ModEntities.THE_SKY, false))
                .then(buildSpawnNode("eyes_of_fun", ModEntities.EYES_OF_FUN, false))
                .then(buildSpawnNode("smile", ModEntities.SMILE, true))
                .then(buildSpawnNode("the_truth", ModEntities.THE_TRUTH, true))
                .then(buildSpawnNode("clock", ModEntities.CLOCK, false));

        // /underworld trigger <event> [<player>]
        LiteralArgumentBuilder<CommandSourceStack> triggerCmd = Commands.literal("trigger")
                .then(Commands.literal("friend_join")
                        .executes(ctx -> executeTriggerFriendJoin(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> executeTriggerFriendJoin(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("friend_chat")
                        .executes(ctx -> executeTriggerFriendChat(ctx.getSource())))
                .then(Commands.literal("hands_encounter")
                        .executes(ctx -> executeTriggerHands(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> executeTriggerHands(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("tree_encounter")
                        .executes(ctx -> executeTriggerTree(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> executeTriggerTree(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("the_sky_event")
                        .executes(ctx -> executeTriggerTheSky(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> executeTriggerTheSky(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("eyes_of_fun")
                        .executes(ctx -> executeTriggerEyesOfFun(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> executeTriggerEyesOfFun(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("smile")
                        .executes(ctx -> executeTriggerSmile(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> executeTriggerSmile(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("the_truth")
                        .executes(ctx -> executeTriggerTheTruth(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> executeTriggerTheTruth(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("clock")
                        .executes(ctx -> executeTriggerClock(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> executeTriggerClock(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))));

        // /underworld tp <overworld|fun_world|maze|the_error|the_time> [<player>]
        LiteralArgumentBuilder<CommandSourceStack> tpCmd = Commands.literal("tp")
                .then(Commands.literal("overworld")
                        .executes(ctx -> executeDimensionTp(ctx.getSource(), ctx.getSource().getPlayerOrException(), "overworld"))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> executeDimensionTp(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), "overworld"))))
                .then(Commands.literal("fun_world")
                        .executes(ctx -> executeDimensionTp(ctx.getSource(), ctx.getSource().getPlayerOrException(), "fun_world"))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> executeDimensionTp(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), "fun_world"))))
                .then(Commands.literal("maze")
                        .executes(ctx -> executeDimensionTp(ctx.getSource(), ctx.getSource().getPlayerOrException(), "maze"))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> executeDimensionTp(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), "maze"))))
                .then(Commands.literal("the_error")
                        .executes(ctx -> executeDimensionTp(ctx.getSource(), ctx.getSource().getPlayerOrException(), "the_error"))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> executeDimensionTp(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), "the_error"))))
                .then(Commands.literal("the_time")
                        .executes(ctx -> executeDimensionTp(ctx.getSource(), ctx.getSource().getPlayerOrException(), "the_time"))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> executeDimensionTp(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), "the_time"))));

        // /underworld structure <name> [<player>]
        LiteralArgumentBuilder<CommandSourceStack> structureCmd = Commands.literal("structure")
                .then(buildStructureNode("void_shrine"))
                .then(buildStructureNode("broken_shaft"))
                .then(buildStructureNode("fountain"))
                .then(buildStructureNode("glitch_cluster"))
                .then(buildStructureNode("elongated_tree"))
                .then(buildStructureNode("wheat_farm"))
                .then(buildStructureNode("old_village_house"))
                .then(buildStructureNode("clock_statue"))
                .then(buildStructureNode("leaf_maze"));

        // /underworld events <enable|disable|status>
        LiteralArgumentBuilder<CommandSourceStack> eventsCmd = Commands.literal("events")
                .then(Commands.literal("enable")
                        .executes(ctx -> executeSetEvents(ctx.getSource(), true)))
                .then(Commands.literal("disable")
                        .executes(ctx -> executeSetEvents(ctx.getSource(), false)))
                .then(Commands.literal("status")
                        .executes(ctx -> executeEventsStatus(ctx.getSource())));

        // /underworld reset friend
        LiteralArgumentBuilder<CommandSourceStack> resetCmd = Commands.literal("reset")
                .then(Commands.literal("friend")
                        .executes(ctx -> executeResetFriend(ctx.getSource())));

        root.then(spawnCmd)
                .then(triggerCmd)
                .then(tpCmd)
                .then(structureCmd)
                .then(eventsCmd)
                .then(resetCmd);

        dispatcher.register(root);
    }

    private static <T extends Mob> LiteralArgumentBuilder<CommandSourceStack> buildSpawnNode(
            String literal, Supplier<EntityType<T>> typeSupplier, boolean allowCave) {
        return Commands.literal(literal)
                .executes(ctx -> executeSpawn(ctx.getSource(), ctx.getSource().getPlayerOrException(), typeSupplier.get(), allowCave))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> executeSpawn(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), typeSupplier.get(), allowCave)));
    }

    private static <T extends Mob> int executeSpawn(CommandSourceStack source, ServerPlayer target,
                                                    EntityType<T> type, boolean allowCave) {
        ServerLevel level = target.serverLevel();
        int minDist = UnderWorldConfigHolder.getSpawnMinDistance();
        int maxDist = UnderWorldConfigHolder.getSpawnMaxDistance();
        BlockPos pos = UnderWorldEventDirector.findSpawnPosAroundPlayer(level, target, minDist, maxDist, allowCave);
        if (pos == null) {
            source.sendFailure(Component.translatable("command.under_world.spawn.failed", target.getName()));
            return 0;
        }

        T entity = type.create(level);
        if (entity == null) {
            source.sendFailure(Component.translatable("command.under_world.spawn.failed", target.getName()));
            return 0;
        }

        entity.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, level.getRandom().nextFloat() * 360.0F, 0.0F);
        entity.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.COMMAND, null, null);
        if (entity instanceof TheTruthEntity truth) {
            truth.setChasedPlayer(target);
        }
        level.addFreshEntity(entity);

        if (entity instanceof FriendEntity) {
            UnderWorldSavedData data = UnderWorldSavedData.get(source.getServer());
            data.setFriendEntityUuid(entity.getUUID());
            if (!data.isFriendInTabList()) {
                data.setFriendInTabList(true);
                NetworkHandler.syncFriendTabToAll(true, UnderWorldConfigHolder.getFriendDisplayName());
            }
        }

        double actualDist = Math.sqrt(target.blockPosition().distSqr(pos));
        source.sendSuccess(() -> Component.translatable(
                "command.under_world.spawn.success",
                entity.getDisplayName(), pos.getX(), pos.getY(), pos.getZ(), actualDist, target.getName()
        ), true);
        return 1;
    }

    private static int executeTriggerFriendJoin(CommandSourceStack source, ServerPlayer target) {
        UnderWorldEventDirector.triggerFriendJoinEvent(source.getServer(), target.serverLevel(), target);
        source.sendSuccess(() -> Component.translatable("command.under_world.trigger.friend_join"), true);
        return 1;
    }

    private static int executeTriggerFriendChat(CommandSourceStack source) {
        String sent = UnderWorldEventDirector.triggerFriendAmbientChat(source.getServer());
        source.sendSuccess(() -> Component.translatable("command.under_world.trigger.friend_chat", sent), true);
        return 1;
    }

    private static int executeTriggerHands(CommandSourceStack source, ServerPlayer target) {
        HandsEntity entity = UnderWorldEventDirector.triggerHandsEncounter(target.serverLevel(), target);
        if (entity == null) {
            source.sendFailure(Component.translatable("command.under_world.spawn.failed", target.getName()));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.under_world.trigger.hands", target.getName()), true);
        return 1;
    }

    private static int executeTriggerTree(CommandSourceStack source, ServerPlayer target) {
        TreeMimicEntity entity = UnderWorldEventDirector.triggerTreeEncounter(target.serverLevel(), target);
        if (entity == null) {
            source.sendFailure(Component.translatable("command.under_world.spawn.failed", target.getName()));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.under_world.trigger.tree", target.getName()), true);
        return 1;
    }

    private static int executeTriggerTheSky(CommandSourceStack source, ServerPlayer target) {
        TheSkyEntity entity = UnderWorldEventDirector.triggerTheSkyEncounter(target.serverLevel(), target, 48, 72);
        if (entity == null) {
            source.sendFailure(Component.translatable("command.under_world.spawn.failed", target.getName()));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.under_world.trigger.the_sky", target.getName()), true);
        return 1;
    }

    private static int executeTriggerEyesOfFun(CommandSourceStack source, ServerPlayer target) {
        EyesOfFunEntity entity = UnderWorldEventDirector.triggerEyesOfFunEncounter(target.serverLevel(), target);
        if (entity == null) {
            source.sendFailure(Component.translatable("command.under_world.spawn.failed", target.getName()));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.under_world.trigger.eyes_of_fun", target.getName()), true);
        return 1;
    }

    private static int executeTriggerSmile(CommandSourceStack source, ServerPlayer target) {
        SmileEntity entity = UnderWorldEventDirector.triggerSmileEncounter(target.serverLevel(), target);
        if (entity == null) {
            source.sendFailure(Component.translatable("command.under_world.spawn.failed", target.getName()));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.under_world.trigger.smile", target.getName()), true);
        return 1;
    }

    private static int executeTriggerTheTruth(CommandSourceStack source, ServerPlayer target) {
        TheTruthEntity entity = UnderWorldEventDirector.triggerTheTruthEncounter(target.serverLevel(), target);
        if (entity == null) {
            source.sendFailure(Component.translatable("command.under_world.spawn.failed", target.getName()));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.under_world.trigger.the_truth", target.getName()), true);
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildStructureNode(String name) {
        return Commands.literal(name)
                .executes(ctx -> executeStructure(ctx.getSource(), ctx.getSource().getPlayerOrException(), name))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> executeStructure(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), name)));
    }

    private static int executeTriggerClock(CommandSourceStack source, ServerPlayer target) {
        ClockEntity entity = UnderWorldEventDirector.triggerClockEncounter(target.serverLevel(), target, ModDimensions.isTime(target.level()));
        if (entity == null) {
            source.sendFailure(Component.translatable("command.under_world.spawn.failed", target.getName()));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.under_world.trigger.clock", target.getName()), true);
        return 1;
    }

    private static int executeDimensionTp(CommandSourceStack source, ServerPlayer target, String dim) {
        boolean ok = switch (dim) {
            case "fun_world" -> ModDimensions.sendToFunWorld(target);
            case "maze" -> ModDimensions.sendToMaze(target);
            case "the_error" -> ModDimensions.sendToError(target);
            case "the_time" -> ModDimensions.sendToTime(target);
            default -> ModDimensions.returnToOverworld(target);
        };
        if (!ok) {
            source.sendFailure(Component.translatable("command.under_world.spawn.failed", target.getName()));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.under_world.dimension.sent", target.getName(), dim), true);
        return 1;
    }

    private static int executeStructure(CommandSourceStack source, ServerPlayer target, String type) throws CommandSyntaxException {
        ServerLevel level = target.serverLevel();
        boolean underground = "broken_shaft".equals(type);
        int minDist = UnderWorldConfigHolder.getSpawnMinDistance();
        int maxDist = UnderWorldConfigHolder.getSpawnMaxDistance();
        BlockPos pos = UnderWorldEventDirector.findSpawnPosAroundPlayer(level, target, minDist, maxDist, underground);
        if (pos == null) {
            pos = target.blockPosition().offset(16, underground ? -12 : 0, 0);
        }
        switch (type) {
            case "void_shrine" -> VoidShrinePiece.placeAt(level, pos);
            case "broken_shaft" -> {
                pos = pos.getY() > 24 ? new BlockPos(pos.getX(), Math.max(level.getMinBuildHeight() + 8, pos.getY() - 18), pos.getZ()) : pos;
                BrokenShaftPiece.placeAt(level, pos);
            }
            case "fountain" -> FountainPiece.placeAt(level, pos.below());
            case "glitch_cluster" -> GlitchClusterPiece.placeAt(level, pos.below());
            case "elongated_tree" -> ElongatedTreePiece.placeAt(level, pos.below());
            case "wheat_farm" -> WheatFarmPiece.placeAt(level, pos.below());
            case "old_village_house" -> OldVillageHousePiece.placeAt(level, pos.below());
            case "clock_statue" -> ClockStatuePiece.placeAt(level, pos.below());
            case "leaf_maze" -> LeafMazePiece.placeAt(level, pos.below());
            default -> {
                source.sendFailure(Component.translatable("command.under_world.spawn.failed", target.getName()));
                return 0;
            }
        }
        BlockPos finalPos = pos;
        source.sendSuccess(() -> Component.translatable(
                "command.under_world.structure.success", "under_world:" + type, finalPos.getX(), finalPos.getY(), finalPos.getZ()
        ), true);
        return 1;
    }

    private static int executeSetEvents(CommandSourceStack source, boolean enable) {
        UnderWorldSavedData data = UnderWorldSavedData.get(source.getServer());
        data.setEventsPaused(!enable);
        source.sendSuccess(() -> Component.translatable(
                enable ? "command.under_world.events.enabled" : "command.under_world.events.disabled"
        ), true);
        return 1;
    }

    private static int executeEventsStatus(CommandSourceStack source) {
        UnderWorldSavedData data = UnderWorldSavedData.get(source.getServer());
        boolean enabled = UnderWorldConfigHolder.isModEnabled() && !data.areEventsPaused();
        ServerLevel funWorld = source.getServer().getLevel(ModDimensions.THE_FUN_WORLD);
        int funCount = funWorld != null ? funWorld.players().size() : 0;
        source.sendSuccess(() -> Component.translatable(
                "command.under_world.events.status",
                enabled,
                data.hasFriendJoined(),
                data.isFriendInTabList(),
                funCount
        ), false);
        return 1;
    }

    private static int executeResetFriend(CommandSourceStack source) {
        UnderWorldSavedData data = UnderWorldSavedData.get(source.getServer());
        data.setFriendHasJoined(false);
        data.setFriendInTabList(false);
        data.setFriendEntityUuid(null);
        NetworkHandler.syncFriendTabToAll(false, UnderWorldConfigHolder.getFriendDisplayName());
        source.sendSuccess(() -> Component.translatable("command.under_world.reset.friend"), true);
        return 1;
    }
}
