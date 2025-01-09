package me.libreh.manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import me.libreh.manhunt.config.Config;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collection;

import static me.libreh.manhunt.utils.Fields.SERVER;
import static me.libreh.manhunt.utils.Methods.*;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RoleCommands {
    public static void hunterCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("hunter")
                .requires(source -> isPreGame())
                .executes(context -> selfHunter(context.getSource()))
                .then(argument("targets", EntityArgumentType.players())
                        .requires(source -> requirePermissionOrOperator(source, "manhunt.hunter"))
                        .executes(context -> setHunters(context.getSource(),
                                EntityArgumentType.getPlayers(context, "targets"))
                        )
                )
        );
    }

    private static int selfHunter(ServerCommandSource source) {
        if (source.isExecutedByPlayer() && (Config.getConfig().gameOptions.presetMode.equals("free_select") ||
                requirePermissionOrOperator(source, "manhunt.hunter"))
        ) {
            var player = source.getPlayer();

            if (isRunner(player)) {
                makeHunter(player);

                SERVER.getPlayerManager().broadcast(Text.translatable("chat.manhunt.joined_team",
                        Text.literal(player.getNameForScoreboard()).formatted(Config.getConfig().gameOptions.teamColor.huntersColor),
                        Text.translatable("role.manhunt.hunters").formatted(Config.getConfig().gameOptions.teamColor.huntersColor)),false
                );
            } else {
                source.sendFeedback(() -> Text.translatable("chat.manhunt.already_team", Text.translatable("role.manhunt.hunter")).formatted(Formatting.RED), false);
            }
        } else {
            source.sendFeedback(() -> Text.translatable("command.unknown.command").formatted(Formatting.RED), false);
            source.sendFeedback(() -> Text.translatable("text.manhunt.both",
                    Text.literal("hunter").styled(style -> style.withUnderline(true)),
                    Text.translatable("command.context.here").formatted(Formatting.RED)), false
            );
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int setHunters(ServerCommandSource source, Collection<ServerPlayerEntity> players) {
        for (ServerPlayerEntity player : players) {
            makeHunter(player);

            source.getServer().getPlayerManager().broadcast(Text.translatable("chat.manhunt.set_role",
                    Text.literal(player.getNameForScoreboard()).formatted(Config.getConfig().gameOptions.teamColor.huntersColor),
                    Text.translatable("role.manhunt.hunter").formatted(Config.getConfig().gameOptions.teamColor.huntersColor)), false
            );
        }

        return Command.SINGLE_SUCCESS;
    }

    public static void oneHunterCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("onehunter")
                .requires(source -> isPreGame() && requirePermissionOrOperator(source, "manhunt.one_hunter"))
                .then(argument("player", EntityArgumentType.player())
                        .executes(context ->
                                setOneHunter(EntityArgumentType.getPlayer(context, "player"))
                        )
                )
        );
    }

    private static int setOneHunter(ServerPlayerEntity player) {
        for (ServerPlayerEntity serverPlayer : SERVER.getPlayerManager().getPlayerList()) {
            makeRunner(serverPlayer);
        }

        makeHunter(player);

        SERVER.getPlayerManager().broadcast(Text.translatable("chat.manhunt.one_role",
                Text.literal(player.getNameForScoreboard()).formatted(Config.getConfig().gameOptions.teamColor.huntersColor),
                Text.translatable("role.manhunt.hunter").formatted(Config.getConfig().gameOptions.teamColor.huntersColor)),
                false
        );

        return Command.SINGLE_SUCCESS;
    }

    public static void oneRunnerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("onerunner")
                .requires(source -> isPreGame() && requirePermissionOrOperator(source, "manhunt.one_runner"))
                .then(argument("player", EntityArgumentType.player())
                        .executes(context ->
                                setOneRunner(EntityArgumentType.getPlayer(context, "player"))
                        )
                )
        );
    }

    private static int setOneRunner(ServerPlayerEntity player) {
        for (ServerPlayerEntity serverPlayer : SERVER.getPlayerManager().getPlayerList()) {
            makeHunter(serverPlayer);
        }

        makeRunner(player);

        SERVER.getPlayerManager().broadcast(Text.translatable("chat.manhunt.one_role",
                Text.literal(player.getNameForScoreboard()).formatted(Config.getConfig().gameOptions.teamColor.runnersColor),
                Text.translatable("role.manhunt.runner").formatted(Config.getConfig().gameOptions.teamColor.runnersColor)),
                false
        );

        return Command.SINGLE_SUCCESS;
    }

    public static void runnerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("runner")
                .requires(source -> isPreGame())
                .executes(context -> selfRunner(context.getSource()))
                .then(argument("targets", EntityArgumentType.players())
                        .requires(source -> requirePermissionOrOperator(source, "manhunt.runner"))
                        .executes(context ->
                                setRunners(EntityArgumentType.getPlayers(context, "targets"))
                        )
                )
        );
    }

    private static int selfRunner(ServerCommandSource source) {
        if (source.isExecutedByPlayer() && (Config.getConfig().gameOptions.presetMode.equals("free_select") ||
                requirePermissionOrOperator(source, "manhunt.runner"))
        ) {
            var player = source.getPlayer();
            if (isHunter(player)) {
                makeRunner(player);

                SERVER.getPlayerManager().broadcast(Text.translatable("chat.manhunt.joined_team",
                        Text.literal(player.getNameForScoreboard()).formatted(Config.getConfig().gameOptions.teamColor.runnersColor),
                        Text.translatable("role.manhunt.runners").formatted(Config.getConfig().gameOptions.teamColor.runnersColor)),
                        false);
            } else {
                source.sendFeedback(() -> Text.translatable("chat.manhunt.already_team",
                        Text.translatable("role.manhunt.runner")).formatted(Formatting.RED), false);
            }
        } else {
            source.sendFeedback(() -> Text.translatable("command.unknown.command").formatted(Formatting.RED), false);
            source.sendFeedback(() -> Text.translatable("text.manhunt.both",
                    Text.literal("runner").styled(style -> style.withUnderline(true)),
                    Text.translatable("command.context.here").formatted(Formatting.RED)), false
            );
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int setRunners(Collection<ServerPlayerEntity> players) {
        for (ServerPlayerEntity player : players) {
            makeRunner(player);

            SERVER.getPlayerManager().broadcast(Text.translatable("chat.manhunt.set_role",
                    Text.literal(player.getNameForScoreboard()).formatted(Config.getConfig().gameOptions.teamColor.runnersColor),
                    Text.translatable("role.manhunt.runner").formatted(Config.getConfig().gameOptions.teamColor.runnersColor)),
                    false
            );
        }

        return Command.SINGLE_SUCCESS;
    }
}
