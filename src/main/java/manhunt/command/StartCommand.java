package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.game.GameState;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.popcraft.chunky.ChunkyProvider;
import org.popcraft.chunky.api.ChunkyAPI;

import static manhunt.ManhuntMod.*;
import static manhunt.game.ManhuntGame.gameStart;

public class StartCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("start")
                .requires(source -> source.isExecutedByPlayer() && state == GameState.PREGAME && checkPermission(source.getPlayer(), "manhunt.start"))
                .executes(context -> executeStart(context.getSource()))
        );
    }

    private static int executeStart(ServerCommandSource source) {
        if (!source.getServer().getScoreboard().getTeam("runners").getPlayerList().isEmpty()) {
            if (chunkyLoaded && config.isChunky()) {
                ChunkyAPI chunky = ChunkyProvider.get().getApi();

                chunky.cancelTask("manhunt:overworld");
                chunky.cancelTask("manhunt:the_nether");
                chunky.cancelTask("manhunt:the_end");
            }

            gameStart(source.getServer());
        } else {
            source.sendFeedback(() -> Text.translatable("chat.minimum", Text.translatable("role.manhunt.runner")).formatted(Formatting.RED), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
