package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import manhunt.ManhuntMod;
import manhunt.config.ManhuntConfig;
import manhunt.game.GameEvents;
import manhunt.game.GameState;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CoordsCommand {
    public static final List<MutableText> hunterCoords = new ArrayList<>();
    public static final List<MutableText> runnerCoords = new ArrayList<>();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("coords")
                .requires(source -> source.isExecutedByPlayer() && ManhuntMod.gameState != GameState.PREGAME)
                .executes(context -> listCoords(context.getSource()))
                .then(CommandManager.argument("message", StringArgumentType.greedyString())
                    .executes(context -> sendCoordsMessage(context.getSource(), StringArgumentType.getString(context, "message")))
                )
        );
    }

    private static int listCoords(ServerCommandSource source) {
        var player = source.getPlayer();
        boolean isHunter = player.isTeamPlayer(player.getScoreboard().getTeam("hunters"));

        if (isHunter && hunterCoords.isEmpty() || !isHunter && runnerCoords.isEmpty()) {
            player.sendMessage(Text.translatable("chat.manhunt.no_coordinates").formatted(Formatting.RED));
        } else {
            Date past;

            Formatting formatting = Formatting.WHITE;

            if (ManhuntConfig.config.isTeamColor()) {
                if (isHunter) {
                    formatting = ManhuntConfig.config.getHuntersColor();
                } else {
                    formatting = ManhuntConfig.config.getRunnersColor();
                }
            }

            player.sendMessage(Text.translatable("chat.manhunt.team_coordinates").styled(style -> style.withBold(true)));

            String team;
            if (isHunter) {
                team = "hunters";
                for (MutableText mutableText : hunterCoords) {
                    String[] array = mutableText.getString().split(" ");

                    past = new Date(Long.parseLong(array[2]));

                    if (array.length > 7) {
                        String dimension = array[6];
                        String message = "";

                        for (int i = 7; i != array.length; i++) {
                            String oldMessage = message;
                            message = oldMessage + array[i];
                        }

                        array[6] = dimension + " " + message;
                    }


                    if (TimeUnit.MILLISECONDS.toHours(new Date().getTime() - past.getTime()) == 0) {
                        if (TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - past.getTime()) == 0) {
                            player.sendMessage(Text.translatable("chat.manhunt.list_coordinates",
                                    Text.literal(team).formatted(formatting),
                                    Text.literal(array[1]).formatted(formatting),
                                    Text.literal(" " + (TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - past.getTime())) + "s "),
                                    Text.literal(array[3]), Text.literal(array[4]), Text.literal(array[5]), Text.literal(array[6]))
                            );
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.list_coordinates",
                                    Text.literal(team).formatted(formatting),
                                    Text.literal(array[1]).formatted(formatting),
                                    Text.literal((TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - past.getTime())) + "m "),
                                    Text.literal(array[3]), Text.literal(array[4]), Text.literal(array[5]), Text.literal(array[6]))
                            );
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.list_coordinates",
                                Text.literal(team).formatted(formatting),
                                Text.literal(array[1]).formatted(formatting),
                                Text.literal((TimeUnit.MILLISECONDS.toHours(new Date().getTime() - past.getTime())) + "h "),
                                Text.literal(array[3]), Text.literal(array[4]), Text.literal(array[5]), Text.literal(array[6]))
                        );
                    }
                }
            } else {
                team = "runners";
                for (MutableText mutableText : runnerCoords) {
                    String[] array = mutableText.getString().split(" ");

                    ManhuntMod.LOGGER.info("Length: " + array.length);
                    past = new Date(Long.parseLong(array[2]));

                    if (array.length > 7) {
                        String dimension = array[6];
                        String message = "";

                        for (int i = 7; i != array.length; i++) {
                            ManhuntMod.LOGGER.info("array[" + i + "]");
                            String oldMessage = message;
                            message = oldMessage + array[i];
                        }

                        array[6] = dimension + " " + message;
                    }

                    if (TimeUnit.MILLISECONDS.toHours(new Date().getTime() - past.getTime()) == 0) {
                        if (TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - past.getTime()) == 0) {
                            player.sendMessage(Text.translatable("chat.manhunt.list_coordinates",
                                    Text.literal(team).formatted(formatting), Text.literal(array[1]).formatted(formatting),
                                    Text.literal(" " + (TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - past.getTime())) + "s "),
                                    Text.literal(array[3]), Text.literal(array[4]), Text.literal(array[5]), Text.literal(array[6]))
                            );
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.list_coordinates",
                                    Text.literal(team).formatted(formatting), Text.literal(array[1]).formatted(formatting),
                                    Text.literal((TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - past.getTime())) + "m"),
                                    Text.literal(array[3]), Text.literal(array[4]), Text.literal(array[5]), Text.literal(array[6]))
                            );
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.list_coordinates",
                                Text.literal(team).formatted(formatting), Text.literal(array[1]).formatted(formatting),
                                Text.literal(" " + (TimeUnit.MILLISECONDS.toHours(new Date().getTime() - past.getTime())) + "h "),
                                Text.literal(array[3]), Text.literal(array[4]), Text.literal(array[5]), Text.literal(array[6]))
                        );
                    }
                }
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int sendCoordsMessage(ServerCommandSource source, String message) {
        var player = source.getPlayer();
        var server = source.getServer();
        var scoreboard = server.getScoreboard();
        boolean isHunter = player.isTeamPlayer(scoreboard.getTeam("hunters"));

        Formatting formatting = Formatting.WHITE;
        if (ManhuntConfig.config.isTeamColor()) {
            if (isHunter) {
                formatting = ManhuntConfig.config.getHuntersColor();
            } else {
                formatting = ManhuntConfig.config.getRunnersColor();
            }
        }

        String team = "hunters";
        if (!isHunter) {
            team = "runners";
        }

        if (player.getWorld().getRegistryKey() != ManhuntMod.overworld.getRegistryKey()) {
            if (player.getWorld().getRegistryKey() == ManhuntMod.theNether.getRegistryKey()) {
                message = "(nether) " + message;
            } else if (player.getWorld().getRegistryKey() == ManhuntMod.theEnd.getRegistryKey()) {
                message = "(end) " + message;
            }
        }

        if (isHunter) {
            for (ServerPlayerEntity serverPlayer : GameEvents.allHunters) {
                serverPlayer.sendMessage(Text.translatable("chat.manhunt.send_coordinates",
                                Text.literal(team).formatted(formatting),
                                Text.literal(player.getNameForScoreboard()).formatted(formatting),
                                Text.literal(String.valueOf(player.getBlockX())),
                                Text.literal(String.valueOf(player.getBlockY())),
                                Text.literal(String.valueOf(player.getBlockZ())),
                                Text.literal(message)
                        )
                );
            }
        } else {
            for (ServerPlayerEntity serverPlayer : GameEvents.allRunners) {
                serverPlayer.sendMessage(Text.translatable("chat.manhunt.send_coordinates",
                                Text.literal(team).formatted(formatting),
                                Text.literal(player.getNameForScoreboard()).formatted(formatting),
                                Text.literal(String.valueOf(player.getBlockX())),
                                Text.literal(String.valueOf(player.getBlockY())),
                                Text.literal(String.valueOf(player.getBlockZ())),
                                Text.literal(message)
                        )
                );
            }
        }

        if (isHunter) {
            hunterCoords.add(Text.translatable("chat.manhunt.save_coordinates",
                            Text.literal(team).formatted(formatting),
                            Text.literal(player.getNameForScoreboard()).formatted(formatting),
                            Text.literal(" " + new Date().getTime() + " "),
                            Text.literal(String.valueOf(player.getBlockX())),
                            Text.literal(String.valueOf(player.getBlockY())),
                            Text.literal(String.valueOf(player.getBlockZ())),
                            Text.literal(message)
                    )
            );
        } else {
            runnerCoords.add(Text.translatable("chat.manhunt.save_coordinates",
                            Text.literal(team).formatted(formatting),
                            Text.literal(player.getNameForScoreboard()).formatted(formatting),
                            Text.literal(" " + new Date().getTime() + " "),
                            Text.literal(String.valueOf(player.getBlockX())),
                            Text.literal(String.valueOf(player.getBlockY())),
                            Text.literal(String.valueOf(player.getBlockZ())),
                            Text.literal(message)
                    )
            );
        }

        return Command.SINGLE_SUCCESS;
    }
}
