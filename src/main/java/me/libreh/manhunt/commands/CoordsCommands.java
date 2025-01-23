package me.libreh.manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.libreh.manhunt.config.Config;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static me.libreh.manhunt.utils.Fields.runnersTeam;
import static me.libreh.manhunt.utils.Fields.server;
import static me.libreh.manhunt.utils.Methods.*;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CoordsCommands {
    public static final List<MutableText> HUNTER_COORDS = new ArrayList<>();
    public static final List<MutableText> RUNNER_COORDS = new ArrayList<>();

    public static void coordsCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("coords")
                .requires(source -> source.isExecutedByPlayer() && !isPreGame())
                .executes(context -> sendCoordsMessage(context.getSource(), ""))
                .then(argument("message", StringArgumentType.greedyString())
                        .executes(context -> sendCoordsMessage(context.getSource(),
                                StringArgumentType.getString(context, "message")))));
    }

    private static int sendCoordsMessage(ServerCommandSource source, String message) {
        var player = source.getPlayer();
        var hunter = isHunter(player);

        Formatting formatting = Formatting.WHITE;

        if (Config.getConfig().gameOptions.teamColor.enabled) {
            if (hunter) {
                formatting = Config.getConfig().gameOptions.teamColor.huntersColor;
            } else {
                formatting = Config.getConfig().gameOptions.teamColor.runnersColor;
            }
        }

        String team = "hunters";
        if (!hunter) {
            team = "runners";
        }

        var world = player.getServerWorld();
        if (!isOverworld(world)) {
            if (isNether(world)) {
                message = "(nether) " + message;
            } else if (isEnd(world)) {
                message = "(end) " + message;
            }
        }

        for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
            if (hunter) {
                if (isHunter(serverPlayer)) {
                    serverPlayer.sendMessage(Text.translatable("chat.manhunt.send_coordinates", Text.literal(team).formatted(formatting),
                            Text.literal(player.getNameForScoreboard()).formatted(formatting),
                            Text.literal(String.valueOf(player.getBlockX())), Text.literal(String.valueOf(player.getBlockY())), Text.literal(String.valueOf(player.getBlockZ())),
                            Text.literal(message))
                    );
                }
            } else {
                if (serverPlayer.isTeamPlayer(runnersTeam)) {
                    serverPlayer.sendMessage(Text.translatable("chat.manhunt.send_coordinates", Text.literal(team).formatted(formatting),
                            Text.literal(player.getNameForScoreboard()).formatted(formatting),
                            Text.literal(String.valueOf(player.getBlockX())), Text.literal(String.valueOf(player.getBlockY())), Text.literal(String.valueOf(player.getBlockZ())),
                            Text.literal(message))
                    );
                }
            }
        }

        if (hunter) {
            HUNTER_COORDS.add(Text.translatable("chat.manhunt.save_coordinates", Text.literal(player.getNameForScoreboard()).formatted(formatting),
                    Text.literal(" " + new Date().getTime() + " "),
                    Text.literal(String.valueOf(player.getBlockX())), Text.literal(String.valueOf(player.getBlockY())), Text.literal(String.valueOf(player.getBlockZ())),
                    Text.literal(message))
            );
        } else {
            RUNNER_COORDS.add(Text.translatable("chat.manhunt.save_coordinates", Text.literal(player.getNameForScoreboard()).formatted(formatting),
                    Text.literal(" " + new Date().getTime() + " "),
                    Text.literal(String.valueOf(player.getBlockX())), Text.literal(String.valueOf(player.getBlockY())), Text.literal(String.valueOf(player.getBlockZ())),
                    Text.literal(message))
            );
        }

        return Command.SINGLE_SUCCESS;
    }

    public static void listCoordsCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("listcoords")
                .requires(source -> source.isExecutedByPlayer() && !isPreGame())
                .executes(context -> listCoords(context.getSource())));
    }

    private static int listCoords(ServerCommandSource source) {
        var player = source.getPlayer();
        var hunter = isHunter(player);

        if (hunter && HUNTER_COORDS.isEmpty() || !hunter && RUNNER_COORDS.isEmpty()) {
            player.sendMessage(Text.translatable("chat.manhunt.no_coordinates").formatted(Formatting.RED));
        } else {
            Date past;

            player.sendMessage(Text.translatable("chat.manhunt.team_coordinates").styled(style -> style.withBold(true)));

            String team;
            if (hunter) {
                team = "hunters";
                for (MutableText mutableText : HUNTER_COORDS) {
                    String[] array = mutableText.getString().split(" ");

                    past = new Date(Long.parseLong(array[1]));

                    String message = "";
                    if (array.length > 4) {
                        for (int i = 5; i < array.length; i++) {
                            String oldMessage;
                            if (message.isEmpty()) {
                                oldMessage = message;
                            } else {
                                oldMessage = message + " ";
                            }
                            message = oldMessage + array[i];
                        }

                        array[4] = array[4] + "] " + message;
                    }

                    if (TimeUnit.MILLISECONDS.toHours(new Date().getTime() - past.getTime()) == 0) {
                        if (TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - past.getTime()) == 0) {
                            player.sendMessage(Text.translatable("chat.manhunt.list_coordinates",
                                    Text.literal(team), Text.literal(array[0]),
                                    Text.literal(" " +
                                            (TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - past.getTime())) + "s "),
                                    Text.literal(array[2]), Text.literal(array[3]), Text.literal(array[4])).formatted(Formatting.GRAY)
                            );
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.list_coordinates",
                                    Text.literal(team), Text.literal(array[0]),
                                    Text.literal(" " +
                                            (TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - past.getTime())) + "m "),
                                    Text.literal(array[2]), Text.literal(array[3]), Text.literal(array[4])).formatted(Formatting.GRAY)
                            );
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.list_coordinates",
                                Text.literal(team), Text.literal(array[0]),
                                Text.literal(" " + (
                                        TimeUnit.MILLISECONDS.toHours(new Date().getTime() - past.getTime())) + "h "),
                                Text.literal(array[2]), Text.literal(array[3]), Text.literal(array[4])).formatted(Formatting.GRAY)
                        );
                    }
                }
            } else {
                team = "runners";
                for (MutableText mutableText : RUNNER_COORDS) {
                    String[] array = mutableText.getString().split(" ");

                    past = new Date(Long.parseLong(array[1]));

                    String message = "";
                    if (array.length > 4) {
                        for (int i = 5; i < array.length; i++) {
                            String oldMessage;
                            if (message.isEmpty()) {
                                oldMessage = message;
                            } else {
                                oldMessage = message + " ";
                            }
                            message = oldMessage + array[i];
                        }

                        array[4] = array[4] + "] " + message;
                    }

                    if (TimeUnit.MILLISECONDS.toHours(new Date().getTime() - past.getTime()) == 0) {
                        if (TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - past.getTime()) == 0) {
                            player.sendMessage(Text.translatable("chat.manhunt.list_coordinates",
                                    Text.literal(team), Text.literal(array[0]),
                                    Text.literal(" " + (
                                            TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - past.getTime())) + "s "),
                                    Text.literal(array[2]), Text.literal(array[3]), Text.literal(array[4])).formatted(Formatting.GRAY)
                            );
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.list_coordinates",
                                    Text.literal(team),
                                    Text.literal(array[0]),
                                    Text.literal(" " + (
                                            TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - past.getTime())) + "m "),
                                    Text.literal(array[2]), Text.literal(array[3]), Text.literal(array[4])).formatted(Formatting.GRAY)
                            );
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.list_coordinates",
                                Text.literal(team), Text.literal(array[0]),
                                Text.literal(" " + (
                                        TimeUnit.MILLISECONDS.toHours(new Date().getTime() - past.getTime())) + "h "),
                                Text.literal(array[2]), Text.literal(array[3]), Text.literal(array[4])).formatted(Formatting.GRAY)
                        );
                    }
                }
            }
        }

        return Command.SINGLE_SUCCESS;
    }
}
