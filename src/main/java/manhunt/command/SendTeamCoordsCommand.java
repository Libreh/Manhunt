package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.util.MessageUtil;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

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
                MessageUtil.sendMessage(player, "manhunt.chat.sendhuntercoords", player.getName().getString(), (int) player.getX(), (int) player.getY(), (int) player.getZ());
                MessageUtil.sendMessageToTeam("hunters", "manhunt.chat.huntercoords", player.getName().getString(), (int) player.getX(), (int) player.getY(), (int) player.getZ());
            } else {
                MessageUtil.sendMessage(player, "manhunt.chat.sendhrunnercoords", player.getName().getString(), (int) player.getX(), (int) player.getY(), (int) player.getZ());
                MessageUtil.sendMessageToTeam("runners", "manhunt.chat.runnercoords", player.getName().getString(), (int) player.getX(), (int) player.getY(), (int) player.getZ());
            }
        } else {
            source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.pregame"), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
