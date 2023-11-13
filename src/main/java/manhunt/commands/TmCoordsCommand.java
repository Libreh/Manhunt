package manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.GameState;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static manhunt.Manhunt.gameState;
import static net.minecraft.server.command.CommandManager.literal;

public class TmCoordsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("tmcoords")
                .executes(context -> sendTeamCoords(context.getSource()))
        );
    }

    private static int sendTeamCoords(ServerCommandSource source) {
        if (gameState == GameState.PLAYING) {
            if (source.getPlayer().getScoreboardTeam().isEqual(source.getPlayer().getScoreboard().getTeam("hunters"))) {
                for (String playerName : source.getServer().getScoreboard().getTeam("hunters").getPlayerList()) {
                    source.getServer().getPlayerManager().getPlayer(playerName).sendMessage(Text.translatable("manhunt.chat.huntercoords", Text.literal(source.getPlayer().getName().getString()).formatted(Formatting.RED), (int) source.getPlayer().getX(), (int) source.getPlayer().getY(), (int) source.getPlayer().getZ()));
                }
            } else {
                for (String playerName : source.getServer().getScoreboard().getTeam("runners").getPlayerList()) {
                    source.getServer().getPlayerManager().getPlayer(playerName).sendMessage(Text.translatable("manhunt.chat.runnercoords", Text.literal(source.getPlayer().getName().getString()).formatted(Formatting.GREEN), (int) source.getPlayer().getX(), (int) source.getPlayer().getY(), (int) source.getPlayer().getZ()));
                }
            }
        } else {
            source.sendFeedback(() -> Text.translatable("manhunt.chat.done"), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
