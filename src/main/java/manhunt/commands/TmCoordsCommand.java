package manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.game.ManhuntState;
import manhunt.util.MessageUtil;
import net.minecraft.server.command.ServerCommandSource;

import static manhunt.game.ManhuntGame.gameState;
import static net.minecraft.server.command.CommandManager.literal;

public class TmCoordsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("tmcoords")
                .executes(context -> sendTeamCoords(context.getSource()))
        );
    }

    private static int sendTeamCoords(ServerCommandSource source) {
        if (gameState == ManhuntState.PLAYING) {
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
            source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.done"), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
