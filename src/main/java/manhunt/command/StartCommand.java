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
import java.util.Collections;
import java.util.function.Predicate;

public class StartCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("start")
                .executes(context -> startCommand(context.getSource()))
        );
    }

    private static int startCommand(ServerCommandSource source) {
        if (Arrays.stream(Manhunt.SERVER.getPlayerManager().getWhitelistedNames()).anyMatch(Predicate.isEqual(source.getName().toString()))) {
            if (ManhuntGame.gameState == ManhuntState.PREGAME) {
                if (Collections.frequency(ManhuntGame.currentRole.values(), "runner") == 0) {
                    source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.minimum"), false);
                } else if (Collections.frequency(ManhuntGame.currentRole.values(), "runner") >= 1) {
                    ManhuntGame.startGame(source.getServer());
                }
            }
        } else {
            source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.player"), false);
        }
        return Command.SINGLE_SUCCESS;
    }
}
