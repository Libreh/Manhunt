package me.libreh.manhunt.command.game.coords;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static me.libreh.manhunt.config.ManhuntConfig.CONFIG;
import static me.libreh.manhunt.utils.Fields.RUNNERS_TEAM;
import static me.libreh.manhunt.utils.Fields.SERVER;
import static me.libreh.manhunt.utils.Methods.*;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CoordsCommand {
    public static final List<MutableText> HUNTER_COORDS = new ArrayList<>();
    public static final List<MutableText> RUNNER_COORDS = new ArrayList<>();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
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
        if (CONFIG.isTeamColor()) {
            if (hunter) {
                formatting = CONFIG.getHuntersColor();
            } else {
                formatting = CONFIG.getRunnersColor();
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

        for (ServerPlayerEntity serverPlayer : SERVER.getPlayerManager().getPlayerList()) {
            if (hunter) {
                if (isHunter(serverPlayer)) {
                    serverPlayer.sendMessage(Text.translatable("chat.manhunt.send_coordinates", Text.literal(team).formatted(formatting),
                            Text.literal(player.getNameForScoreboard()).formatted(formatting),
                            Text.literal(String.valueOf(player.getBlockX())), Text.literal(String.valueOf(player.getBlockY())), Text.literal(String.valueOf(player.getBlockZ())),
                            Text.literal(message)));
                }
            } else {
                if (serverPlayer.isTeamPlayer(RUNNERS_TEAM)) {
                    serverPlayer.sendMessage(Text.translatable("chat.manhunt.send_coordinates", Text.literal(team).formatted(formatting),
                            Text.literal(player.getNameForScoreboard()).formatted(formatting),
                            Text.literal(String.valueOf(player.getBlockX())), Text.literal(String.valueOf(player.getBlockY())), Text.literal(String.valueOf(player.getBlockZ())),
                            Text.literal(message)));
                }
            }
        }

        if (hunter) {
            HUNTER_COORDS.add(Text.translatable("chat.manhunt.save_coordinates", Text.literal(player.getNameForScoreboard()).formatted(formatting),
                    Text.literal(" " + new Date().getTime() + " "),
                    Text.literal(String.valueOf(player.getBlockX())), Text.literal(String.valueOf(player.getBlockY())), Text.literal(String.valueOf(player.getBlockZ())),
                    Text.literal(message)));
        } else {
            RUNNER_COORDS.add(Text.translatable("chat.manhunt.save_coordinates", Text.literal(player.getNameForScoreboard()).formatted(formatting),
                    Text.literal(" " + new Date().getTime() + " "),
                    Text.literal(String.valueOf(player.getBlockX())), Text.literal(String.valueOf(player.getBlockY())), Text.literal(String.valueOf(player.getBlockZ())),
                    Text.literal(message)));
        }

        return Command.SINGLE_SUCCESS;
    }
}
