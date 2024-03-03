package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static manhunt.game.ManhuntGame.gameState;
import static manhunt.game.ManhuntState.PLAYING;

public class SendTeamCoordsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("sendteamcoords")
                .executes(context -> sendTeamCoords(context.getSource()))
        );
    }

    private static int sendTeamCoords(ServerCommandSource source) {
        if (gameState == PLAYING) {
            ServerPlayerEntity player = source.getPlayer();
            Scoreboard scoreboard = player.getScoreboard();

            if (player.isTeamPlayer(scoreboard.getTeam("hunters"))) {
                player.sendMessage(Text.translatable("manhunt.chat.sendteamcoords", Text.literal("[hunters]").formatted(Formatting.RED), Text.literal(player.getName().getString()).formatted(Formatting.RED), Text.literal(String.valueOf(player.getX())), Text.literal(String.valueOf(player.getY())), Text.literal(String.valueOf(player.getZ()))));
                for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
                    if (serverPlayer.isTeamPlayer(scoreboard.getTeam("hunters"))) {
                        player.sendMessage(Text.translatable("manhunt.chat.teamcoords", Text.literal("[hunters]").formatted(Formatting.RED), Text.literal(player.getName().getString()).formatted(Formatting.RED), Text.literal(String.valueOf(player.getX())), Text.literal(String.valueOf(player.getY())), Text.literal(String.valueOf(player.getZ()))));
                    }
                }
            } else {
                player.sendMessage(Text.translatable("manhunt.chat.sendteamcoords", Text.literal("[runners]").formatted(Formatting.GREEN), Text.literal(player.getName().getString()).formatted(Formatting.GREEN), Text.literal(String.valueOf(player.getX())), Text.literal(String.valueOf(player.getY())), Text.literal(String.valueOf(player.getZ()))));
                for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
                    if (serverPlayer.isTeamPlayer(scoreboard.getTeam("runners"))) {
                        player.sendMessage(Text.translatable("manhunt.chat.teamcoords", Text.literal("[runners]").formatted(Formatting.GREEN), Text.literal(player.getName().getString()).formatted(Formatting.GREEN), Text.literal(String.valueOf(player.getX())), Text.literal(String.valueOf(player.getY())), Text.literal(String.valueOf(player.getZ()))));
                    }
                }
            }
        } else {
            source.sendFeedback(() -> Text.translatable("manhunt.chat.postgame").formatted(Formatting.RED), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
