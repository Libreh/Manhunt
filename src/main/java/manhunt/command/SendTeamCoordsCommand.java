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
import net.minecraft.server.network.ServerPlayerEntity;

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
            ServerPlayerEntity player = source.getPlayer();

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
