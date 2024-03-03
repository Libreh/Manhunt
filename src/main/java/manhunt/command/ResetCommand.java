package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static manhunt.game.ManhuntGame.gameState;
import static manhunt.game.ManhuntGame.resetGame;
import static manhunt.game.ManhuntState.PLAYING;
import static manhunt.game.ManhuntState.POSTGAME;

public class ResetCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("reset")
                .executes(context -> resetCommand(context.getSource()))
        );
    }

    private static int resetCommand(ServerCommandSource source) {
        if (gameState == PLAYING || gameState == POSTGAME) {
            if (source.hasPermissionLevel(1) || source.hasPermissionLevel(2) || source.hasPermissionLevel(3) || source.hasPermissionLevel(4)) {
                resetGame(source.getServer());
            } else {
                source.sendFeedback(() -> Text.translatable("manhunt.chat.onlyleader").formatted(Formatting.RED), false);
            }
        } else {
            source.sendFeedback(() -> Text.translatable("manhunt.chat.pregame").formatted(Formatting.RED), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
