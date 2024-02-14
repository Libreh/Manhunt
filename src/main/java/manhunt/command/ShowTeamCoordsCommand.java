package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import manhunt.Manhunt;
import manhunt.game.ManhuntGame;
import manhunt.game.ManhuntState;
import manhunt.util.MessageUtil;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class ShowTeamCoordsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("showteamcoords")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .suggests(playersInTeam())
                        .executes(context -> showTeamCoords(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                )
        );
    }

    public static SuggestionProvider<ServerCommandSource> playersInTeam() {
        return (ctx, builder) -> {
            ServerPlayerEntity source = ctx.getSource().getPlayer();
            if (source == null) {
                return Suggestions.empty();
            }

            for (ServerPlayerEntity player : source.server.getPlayerManager().getPlayerList()) {
                if (player.isTeammate(source)) {
                    builder.suggest(player.getNameForScoreboard());
                }
            }

            return builder.buildFuture();
        };
    }

    private static int showTeamCoords(ServerCommandSource source, ServerPlayerEntity player) {
        if (player.isTeamPlayer(Manhunt.SERVER.getScoreboard().getTeam(source.getPlayer().getScoreboardTeam().getName()))) {
            if (ManhuntGame.gameState == ManhuntState.PLAYING) {
                MinecraftServer server = Manhunt.SERVER;
                Scoreboard scoreboard = server.getScoreboard();

                if (source.getPlayer().getScoreboardTeam().isEqual(scoreboard.getTeam("hunters"))) {
                    if (player.isTeamPlayer(scoreboard.getTeam("hunters"))) {
                        MessageUtil.sendMessage(source.getPlayer(), "manhunt.chat.huntercoords", player.getName().getString(), (int) player.getX(), (int) player.getY(), (int) player.getZ());
                    }
                } else {
                    if (player.isTeamPlayer(scoreboard.getTeam("runners"))) {
                        MessageUtil.sendMessage(source.getPlayer(), "manhunt.chat.runnercoords", player.getName().getString(), (int) player.getX(), (int) player.getY(), (int) player.getZ());
                    }
                }
            } else {
                source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.pregame"), false);
            }
        }
        return Command.SINGLE_SUCCESS;
    }
}
