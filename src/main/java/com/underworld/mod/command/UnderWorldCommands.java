package com.underworld.mod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.underworld.mod.config.UnderWorldConfigHolder;
import com.underworld.mod.entity.ClockEntity;
import com.underworld.mod.entity.DemonFishEntity;
import com.underworld.mod.entity.EyesOfFunEntity;
import com.underworld.mod.entity.FriendEntity;
import com.underworld.mod.entity.HandsEntity;
import com.underworld.mod.entity.LivingPylonEntity;
import com.underworld.mod.entity.SmileEntity;
import com.underworld.mod.entity.TheBallEntity;
import com.underworld.mod.entity.TheDotComEntity;
import com.underworld.mod.entity.TheMimicEntity;
import com.underworld.mod.entity.TheSkyEntity;
import com.underworld.mod.entity.TheTruthEntity;
import com.underworld.mod.entity.TreeMimicEntity;
import com.underworld.mod.entity.WindTurbineStalkerEntity;
import com.underworld.mod.event.UnderWorldEventDirector;
import com.underworld.mod.network.NetworkHandler;
import com.underworld.mod.registry.ModDimensions;
import com.underworld.mod.registry.ModEntities;
import com.underworld.mod.world.UnderWorldSavedData;
import com.underworld.mod.world.structure.lore.LoreStructureSpawner;
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
                .then(buildSpawnNode("clock", ModEntities.CLOCK, false))
                .then(buildSpawnNode("the_mimic", ModEntities.THE_MIMIC, false))
                .then(buildSpawnNode("demon_fish", ModEntities.DEMON_FISH, false))
                .then(buildSpawnNode("the_ball", ModEntities.THE_BALL, false))
                .then(buildSpawnNode("living_pylon", ModEntities.LIVING_PYLON, false))
                .then(buildSpawnNode("wind_turbine_stalker", ModEntities.WIND_TURBINE_STALKER, false))
                .then(buildSpawnNode("the_dot_com", ModEntities.THE_DOT_COM, false));

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
                                .executes(ctx -> executeTriggerClock(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("the_mimic")
                        .executes(ctx -> executeTriggerTheMimic(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> executeTriggerTheMimic(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("demon_fish")
                        .executes(ctx -> executeTriggerDemonFish(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> executeTriggerDemonFish(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("the_ball")
                        .executes(ctx -> executeTriggerTheBall(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> executeTriggerTheBall(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("living_pylon")
                        .executes(ctx -> executeTriggerLivingPylon(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> executeTriggerLivingPylon(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("wind_turbine")
                        .executes(ctx -> executeTriggerWindTurbine(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> executeTriggerWindTurbine(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("the_dot_com")
                        .executes(ctx -> executeTriggerDotCom(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> executeTriggerDotCom(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("welcome_to_the_hell")
                        .executes(ctx -> executeTriggerWelcomeToTheHell(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> executeTriggerWelcomeToTheHell(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))));

        // /underworld tp <overworld|fun_world|maze|the_error|the_time|the_plains|the_wind_ocean|the_xp|the_browser> [<player>]
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
                                .executes(ctx -> executeDimensionTp(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), "the_time"))))
                .then(Commands.literal("the_plains")
                        .executes(ctx -> executeDimensionTp(ctx.getSource(), ctx.getSource().getPlayerOrException(), "the_plains"))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> executeDimensionTp(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), "the_plains"))))
                .then(Commands.literal("the_wind_ocean")
                        .executes(ctx -> executeDimensionTp(ctx.getSource(), ctx.getSource().getPlayerOrException(), "the_wind_ocean"))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> executeDimensionTp(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), "the_wind_ocean"))))
                .then(Commands.literal("the_xp")
                        .executes(ctx -> executeDimensionTp(ctx.getSource(), ctx.getSource().getPlayerOrException(), "the_xp"))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> executeDimensionTp(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), "the_xp"))))
                .then(Commands.literal("the_browser")
                        .executes(ctx -> executeDimensionTp(ctx.getSource(), ctx.getSource().getPlayerOrException(), "the_browser"))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> executeDimensionTp(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), "the_browser"))));

        // /underworld structure <name> [<player>] (all classic + lore + v2.4.0 structures)
        LiteralArgumentBuilder<CommandSourceStack> structureCmd = Commands.literal("structure")
                .then(buildStructureNode("void_shrine"))
                .then(buildStructureNode("broken_shaft"))
                .then(buildStructureNode("fountain"))
                .then(buildStructureNode("glitch_cluster"))
                .then(buildStructureNode("elongated_tree"))
                .then(buildStructureNode("wheat_farm"))
                .then(buildStructureNode("old_village_house"))
                .then(buildStructureNode("clock_statue"))
                .then(buildStructureNode("leaf_maze"))
                .then(buildStructureNode("power_pylon"))
                .then(buildStructureNode("hillside_house"))
                .then(buildStructureNode("clan_base"))
                .then(buildStructureNode("mario_pixel_art"))
                .then(buildStructureNode("server_monument"))
                .then(buildStructureNode("abandoned_treehouse"))
                .then(buildStructureNode("secret_dugout"))
                .then(buildStructureNode("old_spawn_hub"))
                .then(buildStructureNode("villager_trading_hall"))
                .then(buildStructureNode("computer_desk"));

        // /underworld lore <name> [<player>] (server-lore structures + random)
        LiteralArgumentBuilder<CommandSourceStack> loreCmd = Commands.literal("lore")
                .then(buildStructureNode("hillside_house"))
                .then(buildStructureNode("clan_base"))
                .then(buildStructureNode("mario_pixel_art"))
                .then(buildStructureNode("server_monument"))
                .then(buildStructureNode("abandoned_treehouse"))
                .then(buildStructureNode("secret_dugout"))
                .then(buildStructureNode("old_spawn_hub"))
                .then(buildStructureNode("villager_trading_hall"))
                .then(buildStructureNode("computer_desk"))
                .then(Commands.literal("random")
                        .executes(ctx -> executeRandomLoreStructure(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> executeRandomLoreStructure(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))));

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
                .then(loreCmd)
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
        BlockPos pos;
        if (type == ModEntities.DEMON_FISH.get()) {
            pos = UnderWorldEventDirector.findWaterSpawnPosAroundPlayer(level, target, 8, Math.max(12, maxDist));
            if (pos == null) {
                pos = UnderWorldEventDirector.findSpawnPosAroundPlayer(level, target, minDist, maxDist, false);
            }
        } else if (type == ModEntities.WIND_TURBINE_STALKER.get()) {
            pos = UnderWorldEventDirector.findWaterSpawnPosAroundPlayer(level, target, minDist, Math.max(24, maxDist));
            if (pos == null) {
                pos = UnderWorldEventDirector.findSpawnPosAroundPlayer(level, target, minDist, maxDist, false);
            }
        } else {
            pos = UnderWorldEventDirector.findSpawnPosAroundPlayer(level, target, minDist, maxDist, allowCave);
        }
        if (pos == null) {
            source.sendFailure(Component.translatable("command.under_world.spawn.failed", target.getName()));
            return 0;
        }

        T entity = type.create(level);
        if (entity == null) {
            source.sendFailure(Component.translatable("command.under_world.spawn.failed", target.getName()));
            return 0;
        }

        double spawnY = (entity instanceof TheBallEntity) ? pos.getY() + 1.8D : pos.getY();
        entity.moveTo(pos.getX() + 0.5D, spawnY, pos.getZ() + 0.5D, level.getRandom().nextFloat() * 360.0F, 0.0F);
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

        final BlockPos spawnPos = pos;
        double actualDist = Math.sqrt(target.blockPosition().distSqr(spawnPos));
        source.sendSuccess(() -> Component.translatable(
                "command.under_world.spawn.success",
                entity.getDisplayName(), spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), actualDist, target.getName()
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

    private static int executeTriggerTheMimic(CommandSourceStack source, ServerPlayer target) {
        TheMimicEntity entity = UnderWorldEventDirector.triggerTheMimicEncounter(target.serverLevel(), target);
        if (entity == null) {
            source.sendFailure(Component.translatable("command.under_world.spawn.failed", target.getName()));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.under_world.trigger.the_mimic", target.getName()), true);
        return 1;
    }

    private static int executeTriggerDemonFish(CommandSourceStack source, ServerPlayer target) {
        DemonFishEntity entity = UnderWorldEventDirector.triggerDemonFishEncounter(target.serverLevel(), target);
        if (entity == null) {
            source.sendFailure(Component.translatable("command.under_world.spawn.failed", target.getName()));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.under_world.trigger.demon_fish", target.getName()), true);
        return 1;
    }

    private static int executeTriggerTheBall(CommandSourceStack source, ServerPlayer target) {
        TheBallEntity entity = UnderWorldEventDirector.triggerTheBallEncounter(target.serverLevel(), target);
        if (entity == null) {
            source.sendFailure(Component.translatable("command.under_world.spawn.failed", target.getName()));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.under_world.trigger.the_ball", target.getName()), true);
        return 1;
    }

    private static int executeTriggerLivingPylon(CommandSourceStack source, ServerPlayer target) {
        LivingPylonEntity entity = UnderWorldEventDirector.triggerLivingPylonEncounter(target.serverLevel(), target);
        if (entity == null) {
            source.sendFailure(Component.translatable("command.under_world.spawn.failed", target.getName()));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.under_world.trigger.living_pylon", target.getName()), true);
        return 1;
    }

    private static int executeTriggerWindTurbine(CommandSourceStack source, ServerPlayer target) {
        WindTurbineStalkerEntity entity = UnderWorldEventDirector.triggerWindTurbineEncounter(target.serverLevel(), target, true);
        if (entity == null) {
            source.sendFailure(Component.translatable("command.under_world.spawn.failed", target.getName()));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.under_world.trigger.wind_turbine", target.getName()), true);
        return 1;
    }

    private static int executeTriggerDotCom(CommandSourceStack source, ServerPlayer target) {
        TheDotComEntity entity = UnderWorldEventDirector.triggerDotComEncounter(target.serverLevel(), target);
        if (entity == null) {
            source.sendFailure(Component.translatable("command.under_world.spawn.failed", target.getName()));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.under_world.trigger.the_dot_com", target.getName()), true);
        return 1;
    }

    private static int executeTriggerWelcomeToTheHell(CommandSourceStack source, ServerPlayer target) {
        UnderWorldEventDirector.triggerWelcomeToTheHellEvent(source.getServer(), target);
        source.sendSuccess(() -> Component.translatable("command.under_world.trigger.welcome_to_the_hell", target.getName()), true);
        return 1;
    }

    private static int executeRandomLoreStructure(CommandSourceStack source, ServerPlayer target) {
        String id = LoreStructureSpawner.LORE_STRUCTURE_IDS[
                target.serverLevel().getRandom().nextInt(LoreStructureSpawner.LORE_STRUCTURE_IDS.length)
        ];
        return executeStructure(source, target, id);
    }

    private static int executeDimensionTp(CommandSourceStack source, ServerPlayer target, String dim) {
        boolean ok = switch (dim) {
            case "fun_world" -> ModDimensions.sendToFunWorld(target);
            case "maze" -> ModDimensions.sendToMaze(target);
            case "the_error" -> ModDimensions.sendToError(target);
            case "the_time" -> ModDimensions.sendToTime(target);
            case "the_plains" -> ModDimensions.sendToPlains(target);
            case "the_wind_ocean" -> ModDimensions.sendToWindOcean(target);
            case "the_xp" -> ModDimensions.sendToXp(target);
            case "the_browser" -> ModDimensions.sendToBrowser(target);
            default -> ModDimensions.returnToOverworld(target);
        };
        if (!ok) {
            source.sendFailure(Component.translatable("command.under_world.spawn.failed", target.getName()));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.under_world.dimension.sent", target.getName(), dim), true);
        return 1;
    }

    private static int executeStructure(CommandSourceStack source, ServerPlayer target, String type) {
        ServerLevel level = target.serverLevel();
        boolean underground = "broken_shaft".equals(type);
        int minDist = UnderWorldConfigHolder.getSpawnMinDistance();
        int maxDist = UnderWorldConfigHolder.getSpawnMaxDistance();
        BlockPos pos = UnderWorldEventDirector.findSpawnPosAroundPlayer(level, target, minDist, maxDist, underground);
        if (pos == null) {
            pos = target.blockPosition().offset(18, underground ? -12 : 0, 0);
        }
        if (!LoreStructureSpawner.placeById(level, type, pos)) {
            source.sendFailure(Component.translatable("command.under_world.spawn.failed", target.getName()));
            return 0;
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
