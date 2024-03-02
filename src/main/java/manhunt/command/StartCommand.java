package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static manhunt.game.ManhuntGame.*;
import static manhunt.game.ManhuntState.PLAYING;
import static manhunt.game.ManhuntState.PREGAME;

public class StartCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("start")
                .executes(context -> startCommand(context.getSource()))
        );
    }

    private static int startCommand(ServerCommandSource source) {
        ServerPlayerEntity sourcePlayer = source.getPlayer();

        if (gameState == PREGAME) {
            if (hasPreloaded) {
                if (source.hasPermissionLevel(1) || source.hasPermissionLevel(2) || source.hasPermissionLevel(3) || source.hasPermissionLevel(4)) {
                    if (source.getServer().getScoreboard().getTeam("runners").getPlayerList().isEmpty()) {
                        source.sendFeedback(() -> Text.translatable("manhunt.chat.minimum"), false);
                    } else if (!source.getServer().getScoreboard().getTeam("runners").getPlayerList().isEmpty()) {
                        startGame(source.getServer());
                    }
                } else {
                    source.sendFeedback(() -> Text.translatable("manhunt.chat.onlyleader"), false);
                }
            } else {
                source.sendFeedback(() -> Text.translatable("manhunt.chat.preload"), false);
            }
        } else if (gameState == PLAYING) {
            source.sendFeedback(() -> Text.translatable("manhunt.chat.playing"), false);
        } else {
            source.sendFeedback(() -> Text.translatable("manhunt.chat.postgame"), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
