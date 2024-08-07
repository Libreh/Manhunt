package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.ManhuntMod;
import manhunt.config.ManhuntConfig;
import manhunt.game.GameEvents;
import manhunt.game.GameState;
import manhunt.game.ManhuntGame;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.popcraft.chunky.ChunkyProvider;
import org.popcraft.chunky.api.ChunkyAPI;

public class StartCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("start")
                .requires(source -> ManhuntMod.gameState == GameState.PREGAME && GameEvents.startReset && source.isExecutedByPlayer() &&
                        ManhuntMod.checkLeaderPermission(source.getPlayer(), "manhunt.start") || !source.isExecutedByPlayer())
                .executes(context -> executeStart(context.getSource()))
        );
    }

    private static int executeStart(ServerCommandSource source) {
        var server = source.getServer();
        var scoreboard = server.getScoreboard();

        if (!scoreboard.getTeam("runners").getPlayerList().isEmpty()) {
            if (ManhuntGame.chunkyLoaded && ManhuntConfig.config.isChunky()) {
                ChunkyAPI chunky = ChunkyProvider.get().getApi();

                chunky.cancelTask("manhunt:overworld");
                chunky.cancelTask("manhunt:the_nether");
                chunky.cancelTask("manhunt:the_end");
            }

            GameEvents.startingTime = 120;
            GameEvents.starting = true;
        } else {
            source.sendFeedback(() -> Text.translatable("chat.manhunt.minimum", Text.translatable("role.manhunt.runner")).formatted(Formatting.RED), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
