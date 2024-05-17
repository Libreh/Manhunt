package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import manhunt.game.GameState;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static manhunt.ManhuntMod.*;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CoordsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("coords")
                .requires(source -> getGameState() == GameState.PLAYING)
                .executes(context -> listCoords(context.getSource()))
                .then(argument("message", StringArgumentType.greedyString())
                    .executes(context -> sendCoordsMessage(context.getSource(), StringArgumentType.getString(context, "message")))
                )
        );
    }

    private static int listCoords(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();

        boolean isHunter = player.getScoreboardTeam().getName().equals("hunters");

        if ((isHunter && hunterCoords.isEmpty()) || !isHunter && runnerCoords.isEmpty()) {
            player.sendMessage(Text.translatable("manhunt.chat.nocoords").formatted(Formatting.RED));
        } else if ((isHunter && !hunterCoords.isEmpty()) || !isHunter && !runnerCoords.isEmpty()) {
            Date past;

            Formatting formatting = Formatting.WHITE;

            if (config.isTeamColor()) {
                if (isHunter) {
                    formatting = config.getHuntersColor();
                } else {
                    formatting = config.getRunnersColor();
                }
            }

            player.sendMessage(Text.translatable("manhunt.chat.teamcoordinates").formatted(formatting).setStyle(Style.EMPTY.withBold(true)));

            String team = "hunters";

            if (isHunter) {
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
                            player.sendMessage(Text.translatable("manhunt.chat.teamcoords", Text.literal(team).formatted(formatting), Text.literal(array[1]).formatted(formatting), Text.literal(" " + (TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - past.getTime())) + "s" + " "), Text.literal(array[3]), Text.literal(array[4]), Text.literal(array[5]), Text.literal(array[6])));
                        } else {
                            player.sendMessage(Text.translatable("manhunt.chat.teamcoords", Text.literal(team).formatted(formatting), Text.literal(array[1]).formatted(formatting), Text.literal((TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - past.getTime())) + "m"), Text.literal(array[3]), Text.literal(array[4]), Text.literal(array[5]), Text.literal(array[6])));
                        }
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.teamcoords", Text.literal(team).formatted(formatting), Text.literal(array[1]).formatted(formatting), Text.literal(" " + (TimeUnit.MILLISECONDS.toHours(new Date().getTime() - past.getTime())) + "h" + " "), Text.literal(array[3]), Text.literal(array[4]), Text.literal(array[5]), Text.literal(array[6])));
                    }
                }
            } else {
                team = "runners";

                for (MutableText mutableText : runnerCoords) {
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
                            player.sendMessage(Text.translatable("manhunt.chat.teamcoords", Text.literal(team).formatted(formatting), Text.literal(array[1]).formatted(formatting), Text.literal(" " + (TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - past.getTime())) + "s" + " "), Text.literal(array[3]), Text.literal(array[4]), Text.literal(array[5]), Text.literal(array[6])));
                        } else {
                            player.sendMessage(Text.translatable("manhunt.chat.teamcoords", Text.literal(team).formatted(formatting), Text.literal(array[1]).formatted(formatting), Text.literal((TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - past.getTime())) + "m"), Text.literal(array[3]), Text.literal(array[4]), Text.literal(array[5]), Text.literal(array[6])));
                        }
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.teamcoords", Text.literal(team).formatted(formatting), Text.literal(array[1]).formatted(formatting), Text.literal(" " + (TimeUnit.MILLISECONDS.toHours(new Date().getTime() - past.getTime())) + "h" + " "), Text.literal(array[3]), Text.literal(array[4]), Text.literal(array[5]), Text.literal(array[6])));
                    }
                }
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int sendCoordsMessage(ServerCommandSource source, String message) {
        ServerPlayerEntity player = source.getPlayer();

        boolean isHunter = player.getScoreboardTeam().getName().equals("hunters");

        Formatting formatting = Formatting.WHITE;

        if (config.isTeamColor()) {
            if (isHunter) {
                formatting = config.getHuntersColor();
            } else {
                formatting = config.getRunnersColor();
            }
        }

        String team = "hunters";

        if (!isHunter) {
            team = "runners";
        }

        if (player.getWorld() == overworldWorld) {
            for (String playerName : player.getScoreboard().getTeam(team).getPlayerList()) {
                ServerPlayerEntity serverPlayer = player.getServer().getPlayerManager().getPlayer(playerName);
                serverPlayer.sendMessage(Text.translatable("manhunt.chat.coords", Text.literal(team).formatted(formatting), Text.literal(serverPlayer.getName().getString()).formatted(formatting), Text.literal(" "), Text.literal(String.valueOf(Integer.parseInt(String.valueOf((int) serverPlayer.getPos().getX())))), Text.literal(String.valueOf(Integer.parseInt(String.valueOf((int) serverPlayer.getPos().getY())))), Text.literal(String.valueOf(Integer.parseInt(String.valueOf((int) serverPlayer.getPos().getZ())))), Text.literal(message)));
            }

            if (isHunter) {
                hunterCoords.add(Text.translatable("manhunt.chat.savecoords", Text.literal(team).formatted(formatting), Text.literal(player.getName().getString()).formatted(formatting), Text.literal(" " + new Date().getTime() + " "), Text.literal(String.valueOf(Integer.parseInt(String.valueOf((int) player.getPos().getX())))), Text.literal(String.valueOf(Integer.parseInt(String.valueOf((int) player.getPos().getY())))), Text.literal(String.valueOf(Integer.parseInt(String.valueOf((int) player.getPos().getZ())))), Text.literal(message)));
            } else {
                runnerCoords.add(Text.translatable("manhunt.chat.savecoords", Text.literal(team).formatted(formatting), Text.literal(player.getName().getString()).formatted(formatting), Text.literal(" " + new Date().getTime() + " "), Text.literal(String.valueOf(Integer.parseInt(String.valueOf((int) player.getPos().getX())))), Text.literal(String.valueOf(Integer.parseInt(String.valueOf((int) player.getPos().getY())))), Text.literal(String.valueOf(Integer.parseInt(String.valueOf((int) player.getPos().getZ())))), Text.literal(message)));
            }
        } else if (player.getWorld() == netherWorld) {
            for (String playerName : player.getScoreboard().getTeam(team).getPlayerList()) {
                ServerPlayerEntity serverPlayer = player.getServer().getPlayerManager().getPlayer(playerName);
                serverPlayer.sendMessage(Text.translatable("manhunt.chat.coords", Text.literal(team).formatted(formatting), Text.literal(serverPlayer.getName().getString()).formatted(formatting), Text.literal(" "), Text.literal(String.valueOf(Integer.parseInt(String.valueOf((int) serverPlayer.getPos().getX())))), Text.literal(String.valueOf(Integer.parseInt(String.valueOf((int) serverPlayer.getPos().getY())))), Text.literal(String.valueOf(Integer.parseInt(String.valueOf((int) serverPlayer.getPos().getZ())))), Text.literal("(nether) " + message)));
            }

            if (isHunter) {
                hunterCoords.add(Text.translatable("manhunt.chat.savecoords", Text.literal(team).formatted(formatting), Text.literal(player.getName().getString()).formatted(formatting), Text.literal(" " + new Date().getTime() + " "), Text.literal(String.valueOf(Integer.parseInt(String.valueOf((int) player.getPos().getX())))), Text.literal(String.valueOf(Integer.parseInt(String.valueOf((int) player.getPos().getY())))), Text.literal(String.valueOf(Integer.parseInt(String.valueOf((int) player.getPos().getZ())))), Text.literal("(nether) " + message)));
            } else {
                runnerCoords.add(Text.translatable("manhunt.chat.savecoords", Text.literal(team).formatted(formatting), Text.literal(player.getName().getString()).formatted(formatting), Text.literal(" " + new Date().getTime() + " "), Text.literal(String.valueOf(Integer.parseInt(String.valueOf((int) player.getPos().getX())))), Text.literal(String.valueOf(Integer.parseInt(String.valueOf((int) player.getPos().getY())))), Text.literal(String.valueOf(Integer.parseInt(String.valueOf((int) player.getPos().getZ())))), Text.literal("(nether) " + message)));
            }
        } else {
            for (String playerName : player.getScoreboard().getTeam(team).getPlayerList()) {
                ServerPlayerEntity serverPlayer = player.getServer().getPlayerManager().getPlayer(playerName);
                serverPlayer.sendMessage(Text.translatable("manhunt.chat.coords", Text.literal(team).formatted(formatting), Text.literal(serverPlayer.getName().getString()).formatted(formatting), Text.literal(" "), Text.literal(String.valueOf(Integer.parseInt(String.valueOf((int) serverPlayer.getPos().getX())))), Text.literal(String.valueOf(Integer.parseInt(String.valueOf((int) serverPlayer.getPos().getY())))), Text.literal(String.valueOf(Integer.parseInt(String.valueOf((int) serverPlayer.getPos().getZ())))), Text.literal("(end) " + message)));
            }

            if (isHunter) {
                hunterCoords.add(Text.translatable("manhunt.chat.savecoords", Text.literal(team).formatted(formatting), Text.literal(player.getName().getString()).formatted(formatting), Text.literal(" " + new Date().getTime() + " "), Text.literal(String.valueOf(Integer.parseInt(String.valueOf((int) player.getPos().getX())))), Text.literal(String.valueOf(Integer.parseInt(String.valueOf((int) player.getPos().getY())))), Text.literal(String.valueOf(Integer.parseInt(String.valueOf((int) player.getPos().getZ())))), Text.literal("(end) " + message)));
            } else {
                runnerCoords.add(Text.translatable("manhunt.chat.savecoords", Text.literal(team).formatted(formatting), Text.literal(player.getName().getString()).formatted(formatting), Text.literal(" " + new Date().getTime() + " "), Text.literal(String.valueOf(Integer.parseInt(String.valueOf((int) player.getPos().getX())))), Text.literal(String.valueOf(Integer.parseInt(String.valueOf((int) player.getPos().getY())))), Text.literal(String.valueOf(Integer.parseInt(String.valueOf((int) player.getPos().getZ())))), Text.literal("(end) " + message)));
            }
        }

        return Command.SINGLE_SUCCESS;
    }
}
