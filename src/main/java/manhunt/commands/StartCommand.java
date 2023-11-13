package manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.GameState;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Collections;

import static manhunt.Manhunt.*;
import static net.minecraft.server.command.CommandManager.literal;

public class StartCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("start")
                .executes(context -> startCommand(context.getSource()))
        );
    }

    private static int startCommand(ServerCommandSource source) {
        if (gameState == GameState.PREGAME) {
            if (Collections.frequency(currentRole.values(), "runner") == 0) {
                source.sendFeedback(() -> Text.translatable("manhunt.chat.minimum"), false);
            } else if (Collections.frequency(currentRole.values(), "runner") >= 1) {
                startGame(source.getServer());
            }
        }

        return Command.SINGLE_SUCCESS;
    }
}
