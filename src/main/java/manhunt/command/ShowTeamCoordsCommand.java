package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static manhunt.game.ManhuntGame.gameState;
import static manhunt.game.ManhuntState.PLAYING;

public class ShowTeamCoordsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("showteamcoords")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> showTeamCoords(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                        .suggests(playersInTeam())
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
        if (player.isTeamPlayer(player.getScoreboard().getTeam(source.getPlayer().getScoreboardTeam().getName()))) {
            if (gameState == PLAYING) {
                Scoreboard scoreboard = player.getScoreboard();

                if (source.getPlayer().getScoreboardTeam().isEqual(scoreboard.getTeam("hunters"))) {
                    player.sendMessage(Text.translatable("manhunt.chat.teamcoords", Text.literal("[hunters]").formatted(Formatting.RED), Text.literal(player.getName().getString()).formatted(Formatting.RED), Text.literal(String.valueOf(player.getX())), Text.literal(String.valueOf(player.getY())), Text.literal(String.valueOf(player.getZ()))));
                } else {
                    player.sendMessage(Text.translatable("manhunt.chat.teamcoords", Text.literal("[runners]").formatted(Formatting.GREEN), Text.literal(player.getName().getString()).formatted(Formatting.GREEN), Text.literal(String.valueOf(player.getX())), Text.literal(String.valueOf(player.getY())), Text.literal(String.valueOf(player.getZ()))));
                }
            } else {
                source.sendFeedback(() -> Text.translatable("manhunt.chat.pregame").formatted(Formatting.RED), false);
            }
        }
        return Command.SINGLE_SUCCESS;
    }
}
