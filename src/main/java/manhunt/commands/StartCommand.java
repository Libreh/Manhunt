package manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.game.ManhuntState;
import manhunt.util.MessageUtil;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collections;

import static manhunt.game.ManhuntGame.*;
import static net.minecraft.server.command.CommandManager.literal;

public class StartCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("start")
                .executes(context -> startCommand(context.getSource()))
        );
    }

    private static int startCommand(ServerCommandSource source) {
        if (gameState == ManhuntState.PREGAME) {
            if (Collections.frequency(currentRole.values(), "runner") == 0) {
                source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.minimum"), false);
            } else if (Collections.frequency(currentRole.values(), "runner") >= 1) {
                startGame(source.getServer());
            }
        }

        return Command.SINGLE_SUCCESS;
    }
}
