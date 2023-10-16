package manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class TmCoordsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("tmcoords")
                .executes(context -> sendTeamCoords(context.getSource()))
        );
    }

    private static int sendTeamCoords(ServerCommandSource source) {
        for (String playerName : source.getServer().getScoreboard().getPlayerTeam(source.getPlayer().getName().getString()).getPlayerList()) {
            source.getServer().getPlayerManager().getPlayer(playerName).sendMessage(Text.translatable("manhunt.chat.tmcoords", (int) source.getPlayer().getX(), (int) source.getPlayer().getY(), (int) source.getPlayer().getZ()));
        }

        return Command.SINGLE_SUCCESS;
    }
}
