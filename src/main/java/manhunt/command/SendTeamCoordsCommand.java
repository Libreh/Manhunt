package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.Manhunt;
import manhunt.game.ManhuntGame;
import manhunt.game.ManhuntState;
import manhunt.util.MessageUtil;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class SendTeamCoordsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("sendteamcoords")
                .executes(context -> sendTeamCoords(context.getSource()))
        );
    }

    private static int sendTeamCoords(ServerCommandSource source) {
        if (ManhuntGame.gameState == ManhuntState.PLAYING) {
            MinecraftServer server = Manhunt.SERVER;
            Scoreboard scoreboard = server.getScoreboard();

            if (source.getPlayer().getScoreboardTeam().isEqual(scoreboard.getTeam("hunters"))) {
                for (String playerName : scoreboard.getTeam("hunters").getPlayerList()) {
                    MessageUtil.sendMessage(server.getPlayerManager().getPlayer(playerName), "manhunt.chat.huntercoords", source.getPlayer().getName().getString(), (int) source.getPlayer().getX(), (int) source.getPlayer().getY(), (int) source.getPlayer().getZ());
                }
            } else {
                for (String playerName : scoreboard.getTeam("runners").getPlayerList()) {
                    MessageUtil.sendMessage(server.getPlayerManager().getPlayer(playerName), "manhunt.chat.runnercoords", source.getPlayer().getName().getString(), (int) source.getPlayer().getX(), (int) source.getPlayer().getY(), (int) source.getPlayer().getZ());
                }
            }
        } else {
            source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.pregame"), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
