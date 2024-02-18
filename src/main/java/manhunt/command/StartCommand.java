package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.game.ManhuntGame;
import manhunt.game.ManhuntState;
import manhunt.util.MessageUtil;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collections;

public class StartCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("start")
                .executes(context -> startCommand(context.getSource()))
        );
    }

    private static int startCommand(ServerCommandSource source) {
        if (source.hasPermissionLevel(1) || source.hasPermissionLevel(2) || source.hasPermissionLevel(3) || source.hasPermissionLevel(4)) {
            if (ManhuntGame.gameState == ManhuntState.PREGAME) {
                if (Collections.frequency(ManhuntGame.currentRole.values(), "runner") == 0) {
                    source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.minimum"), false);
                } else if (Collections.frequency(ManhuntGame.currentRole.values(), "runner") >= 1) {
                    ManhuntGame.startGame(source.getServer());
                }
            }
        } else {
            source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.leader"), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
