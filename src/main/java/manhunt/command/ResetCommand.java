package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.Manhunt;
import manhunt.game.ManhuntGame;
import manhunt.game.ManhuntState;
import manhunt.util.MessageUtil;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Arrays;
import java.util.function.Predicate;

public class ResetCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("reset")
                .executes(context -> resetCommand(context.getSource()))
        );
    }

    private static int resetCommand(ServerCommandSource source) {
        if (Arrays.stream(Manhunt.SERVER.getPlayerManager().getWhitelistedNames()).anyMatch(Predicate.isEqual(source.getName()))) {
            if (!(ManhuntGame.gameState == ManhuntState.PREGAME)) {
                ManhuntGame.resetGame(source);
            } else {
                source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.pregame"), false);
            }
        } else {
            source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.player"), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
