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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class ShowTeamCoordsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("showteamcoords")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .suggests(playersInTeam())
                        .executes(context -> {
                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                            if (player.isTeamPlayer(Manhunt.SERVER.getScoreboard().getTeam(context.getSource().getPlayer().getScoreboardTeam().getName()))) {
                                if (ManhuntGame.gameState == ManhuntState.PLAYING) {
                                    MinecraftServer server = Manhunt.SERVER;

                                    if (context.getSource().getPlayer().getScoreboardTeam().isEqual(server.getScoreboard().getTeam("hunters"))) {
                                        if (player.isTeamPlayer(server.getScoreboard().getTeam("hunters"))) {
                                            MessageUtil.sendMessage(context.getSource().getPlayer(), "manhunt.chat.huntercoords", player.getName().getString(), (int) player.getX(), (int) player.getY(), (int) player.getZ());
                                        }
                                    } else {
                                        if (player.isTeamPlayer(server.getScoreboard().getTeam("runners"))) {
                                            MessageUtil.sendMessage(context.getSource().getPlayer(), "manhunt.chat.runnercoords", player.getName().getString(), (int) player.getX(), (int) player.getY(), (int) player.getZ());
                                        }
                                    }
                                } else {
                                    context.getSource().sendFeedback(() -> MessageUtil.ofVomponent(context.getSource().getPlayer(), "manhunt.chat.pregame"), false);
                                }
                            }
                            return Command.SINGLE_SUCCESS;
                        }
                ))
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
}
