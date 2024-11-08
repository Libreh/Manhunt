package libreh.manhunt.command.game;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import libreh.manhunt.ManhuntMod;
import libreh.manhunt.config.ManhuntConfig;
import libreh.manhunt.game.GameState;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ListCoordsCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("listcoords")
                .requires(source -> source.isExecutedByPlayer() && ManhuntMod.gameState != GameState.PREGAME)
                .executes(context -> listCoords(context.getSource()))
        );
    }

    private static int listCoords(ServerCommandSource source) {
        var player = source.getPlayer();
        var isHunter = player.isTeamPlayer(player.getScoreboard().getTeam("hunters"));

        if (isHunter && CoordsCommand.HUNTER_COORDS.isEmpty() || !isHunter && CoordsCommand.RUNNER_COORDS.isEmpty()) {
            player.sendMessage(Text.translatable("chat.manhunt.no_coordinates").formatted(Formatting.RED));
        } else {
            Date past;

            Formatting formatting = Formatting.WHITE;

            if (ManhuntConfig.CONFIG.isTeamColor()) {
                if (isHunter) {
                    formatting = ManhuntConfig.CONFIG.getHuntersColor();
                } else {
                    formatting = ManhuntConfig.CONFIG.getRunnersColor();
                }
            }

            player.sendMessage(Text.translatable("chat.manhunt.team_coordinates").styled(style -> style.withBold(true)));

            String team;
            if (isHunter) {
                team = "hunters";
                for (MutableText mutableText : CoordsCommand.HUNTER_COORDS) {
                    String[] array = mutableText.getString().split(" ");

                    past = new Date(Long.parseLong(array[1]));

                    String message = "";
                    for (int i = 5; i < array.length; i++) {
                        String oldMessage;
                        if (message.isEmpty()) {
                            oldMessage = message;
                        } else {
                            oldMessage = message + " ";
                        }
                        message = oldMessage + array[i];
                    }

                    array[5] = message;
                    if (TimeUnit.MILLISECONDS.toHours(new Date().getTime() - past.getTime()) == 0) {
                        if (TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - past.getTime()) == 0) {
                            player.sendMessage(Text.translatable("chat.manhunt.list_coordinates",
                                    Text.literal(team).formatted(formatting),
                                    Text.literal(array[0]).formatted(formatting),
                                    Text.literal(" " + (TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - past.getTime())) + "s "), Text.literal(array[2]), Text.literal(array[3]), Text.literal(array[4]), Text.literal(array[5])));
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.list_coordinates",
                                    Text.literal(team).formatted(formatting),
                                    Text.literal(array[0]).formatted(formatting),
                                    Text.literal(" " + (TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - past.getTime())) + "m "), Text.literal(array[2]), Text.literal(array[3]), Text.literal(array[4]), Text.literal(array[5])));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.list_coordinates",
                                Text.literal(team).formatted(formatting),
                                Text.literal(array[0]).formatted(formatting),
                                Text.literal(" " + (TimeUnit.MILLISECONDS.toHours(new Date().getTime() - past.getTime())) + "h "), Text.literal(array[2]), Text.literal(array[3]), Text.literal(array[4]), Text.literal(array[5])));
                    }
                }
            } else {
                team = "runners";
                for (MutableText mutableText : CoordsCommand.RUNNER_COORDS) {
                    String[] array = mutableText.getString().split(" ");

                    past = new Date(Long.parseLong(array[1]));

                    String message = "";
                    for (int i = 5; i < array.length; i++) {
                        String oldMessage;
                        if (message.isEmpty()) {
                            oldMessage = message;
                        } else {
                            oldMessage = message + " ";
                        }
                        message = oldMessage + array[i];
                    }

                    array[5] = message;
                    if (TimeUnit.MILLISECONDS.toHours(new Date().getTime() - past.getTime()) == 0) {
                        if (TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - past.getTime()) == 0) {
                            player.sendMessage(Text.translatable("chat.manhunt.list_coordinates",
                                    Text.literal(team).formatted(formatting),
                                    Text.literal(array[0]).formatted(formatting),
                                    Text.literal(" " + (TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - past.getTime())) + "s "), Text.literal(array[2]), Text.literal(array[3]), Text.literal(array[4]), Text.literal(array[5])));
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.list_coordinates",
                                    Text.literal(team).formatted(formatting),
                                    Text.literal(array[0]).formatted(formatting),
                                    Text.literal(" " + (TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - past.getTime())) + "m "), Text.literal(array[2]), Text.literal(array[3]), Text.literal(array[4]), Text.literal(array[5])));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.list_coordinates",
                                Text.literal(team).formatted(formatting),
                                Text.literal(array[0]).formatted(formatting),
                                Text.literal(" " + (TimeUnit.MILLISECONDS.toHours(new Date().getTime() - past.getTime())) + "h "), Text.literal(array[2]), Text.literal(array[3]), Text.literal(array[4]), Text.literal(array[5])));
                    }
                }
            }
        }

        return Command.SINGLE_SUCCESS;
    }
}
