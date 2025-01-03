package me.libreh.manhunt.command.game.coords;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static me.libreh.manhunt.command.game.coords.CoordsCommand.HUNTER_COORDS;
import static me.libreh.manhunt.command.game.coords.CoordsCommand.RUNNER_COORDS;
import static me.libreh.manhunt.utils.Methods.isHunter;
import static me.libreh.manhunt.utils.Methods.isPreGame;
import static net.minecraft.server.command.CommandManager.literal;

public class ListCoordsCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
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
                                    Text.literal(array[2]), Text.literal(array[3]), Text.literal(array[4])).formatted(Formatting.GRAY));
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.list_coordinates",
                                    Text.literal(team), Text.literal(array[0]),
                                    Text.literal(" " +
                                            (TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - past.getTime())) + "m "),
                                    Text.literal(array[2]), Text.literal(array[3]), Text.literal(array[4])).formatted(Formatting.GRAY));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.list_coordinates",
                                Text.literal(team), Text.literal(array[0]),
                                Text.literal(" " + (
                                        TimeUnit.MILLISECONDS.toHours(new Date().getTime() - past.getTime())) + "h "),
                                Text.literal(array[2]), Text.literal(array[3]), Text.literal(array[4])).formatted(Formatting.GRAY));
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
                                    Text.literal(array[2]), Text.literal(array[3]), Text.literal(array[4])).formatted(Formatting.GRAY));
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.list_coordinates",
                                    Text.literal(team),
                                    Text.literal(array[0]),
                                    Text.literal(" " + (
                                            TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - past.getTime())) + "m "),
                                    Text.literal(array[2]), Text.literal(array[3]), Text.literal(array[4])).formatted(Formatting.GRAY));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.list_coordinates",
                                Text.literal(team), Text.literal(array[0]),
                                Text.literal(" " + (
                                        TimeUnit.MILLISECONDS.toHours(new Date().getTime() - past.getTime())) + "h "),
                                Text.literal(array[2]), Text.literal(array[3]), Text.literal(array[4])).formatted(Formatting.GRAY));
                    }
                }
            }
        }

        return Command.SINGLE_SUCCESS;
    }
}
