package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.util.MessageUtil;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static manhunt.game.ManhuntGame.gameState;
import static manhunt.game.ManhuntGame.startGame;
import static manhunt.game.ManhuntState.PLAYING;
import static manhunt.game.ManhuntState.PREGAME;

public class StartCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("start")
                .executes(context -> startCommand(context.getSource()))
        );
    }

    private static int startCommand(ServerCommandSource source) {
        if (gameState == PREGAME) {
            if (source.hasPermissionLevel(1) || source.hasPermissionLevel(2) || source.hasPermissionLevel(3) || source.hasPermissionLevel(4)) {
                if (source.getServer().getScoreboard().getTeam("runners").getPlayerList().isEmpty()) {
                    source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.minimum"), false);
                } else if (!source.getServer().getScoreboard().getTeam("runners").getPlayerList().isEmpty()) {
                    startGame(source.getServer());
                }
            } else {
                source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.leader"), false);
            }
        } else if (gameState == PLAYING) {
            source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.playing"), false);
        } else {
            source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.postgame"), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
