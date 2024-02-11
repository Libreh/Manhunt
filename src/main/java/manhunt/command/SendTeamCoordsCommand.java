package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.Manhunt;
import manhunt.game.ManhuntGame;
import manhunt.game.ManhuntState;
import manhunt.util.MessageUtil;
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

            if (source.getPlayer().getScoreboardTeam().isEqual(source.getPlayer().getScoreboard().getTeam("hunters"))) {
                for (String playerName : source.getServer().getScoreboard().getTeam("hunters").getPlayerList()) {
                    MessageUtil.sendMessage(server.getPlayerManager().getPlayer(playerName), "manhunt.chat.huntercoords", source.getPlayer().getName().getString(), (int) source.getPlayer().getX(), (int) source.getPlayer().getY(), (int) source.getPlayer().getZ());
                }
            } else {
                for (String playerName : source.getServer().getScoreboard().getTeam("runners").getPlayerList()) {
                    MessageUtil.sendMessage(server.getPlayerManager().getPlayer(playerName), "manhunt.chat.runnercoords", source.getPlayer().getName().getString(), (int) source.getPlayer().getX(), (int) source.getPlayer().getY(), (int) source.getPlayer().getZ());
                }
            }
        } else {
            source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.pregame"), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
