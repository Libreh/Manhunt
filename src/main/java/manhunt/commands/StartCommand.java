package manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.game.ManhuntGame;
import manhunt.game.ManhuntState;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Collections;

import static manhunt.Manhunt.currentRole;
import static net.minecraft.server.command.CommandManager.literal;

public class StartCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("start")
                .executes(context -> startGame(context.getSource()))
        );
    }

    private static int startGame(ServerCommandSource source) {
        if (ManhuntGame.state == ManhuntState.PREGAME) {
            if (Collections.frequency(currentRole.values(), "runner") == 0) {
                source.sendFeedback(() -> Text.translatable("manhunt.chat.minimum"), false);
            } else if (Collections.frequency(currentRole.values(), "runner") >= 1) {
                ManhuntGame.start(source.getServer());
            }
        }

        return Command.SINGLE_SUCCESS;
    }
}
