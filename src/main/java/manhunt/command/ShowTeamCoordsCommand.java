package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.game.ManhuntGame;
import manhunt.game.ManhuntState;
import manhunt.util.MessageUtil;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class ShowTeamCoordsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("showteamcoords")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> showTeamCoords(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                )
        );
    }

    private static int showTeamCoords(ServerCommandSource source, ServerPlayerEntity player) {
        if (ManhuntGame.gameState == ManhuntState.PLAYING) {
            if (source.getPlayer().getScoreboardTeam().isEqual(source.getPlayer().getScoreboard().getTeam("hunters"))) {
                MessageUtil.sendMessage(source.getPlayer(), "manhunt.chat.huntercoords", player.getName().getString(), (int) player.getX(), (int) player.getY(), (int) player.getZ());
            } else {
                MessageUtil.sendMessage(source.getPlayer(), "manhunt.chat.runnercoords", player.getName().getString(), (int) player.getX(), (int) player.getY(), (int) player.getZ());
            }
        } else {
            source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.pregame"), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
