package manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.game.ManhuntGame;
import manhunt.game.ManhuntState;
import manhunt.util.MessageUtil;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class TmCoordsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("tmcoords")
                .executes(context -> sendTeamCoords(context.getSource()))
        );
    }

    private static int sendTeamCoords(ServerCommandSource source) {
        if (ManhuntGame.gameState == ManhuntState.PLAYING) {
            if (source.getPlayer().getScoreboardTeam().isEqual(source.getPlayer().getScoreboard().getTeam("hunters"))) {
                for (String playerName : source.getServer().getScoreboard().getTeam("hunters").getPlayerList()) {
                    MessageUtil.sendMessage(source.getServer().getPlayerManager().getPlayer(playerName), "manhunt.chat.huntercoords", source.getPlayer().getName().getString(), (int) source.getPlayer().getX(), (int) source.getPlayer().getY(), (int) source.getPlayer().getZ());
                }
            } else {
                for (String playerName : source.getServer().getScoreboard().getTeam("runners").getPlayerList()) {
                    MessageUtil.sendMessage(source.getServer().getPlayerManager().getPlayer(playerName), "manhunt.chat.runtercoords", source.getPlayer().getName().getString(), (int) source.getPlayer().getX(), (int) source.getPlayer().getY(), (int) source.getPlayer().getZ());
                }
            }
        } else {
            source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.pregame"), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
