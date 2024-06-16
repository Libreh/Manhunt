package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.ManhuntMod;
import manhunt.game.GameState;
import manhunt.game.ManhuntGame;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.popcraft.chunky.ChunkyProvider;
import org.popcraft.chunky.api.ChunkyAPI;

import static net.minecraft.server.command.CommandManager.literal;

public class StartCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("start")
                .requires(source -> source.isExecutedByPlayer() && ManhuntMod.getGameState() == GameState.PREGAME && ManhuntMod.checkPermission(source.getPlayer(), "manhunt.start"))
                .executes(context -> executeStart(context.getSource()))
        );
    }

    private static int executeStart(ServerCommandSource source) {
        if (!source.getServer().getScoreboard().getTeam("runners").getPlayerList().isEmpty()) {
            if (ManhuntMod.isChunkyIntegration() && !ManhuntMod.isPreloaded()) {
                ChunkyAPI chunky = ChunkyProvider.get().getApi();

                chunky.cancelTask("manhunt:overworld");
                chunky.cancelTask("manhunt:the_nether");
            }

            ManhuntGame.startGame(source.getServer());
        } else if (source.getServer().getScoreboard().getTeam("runners").getPlayerList().isEmpty()) {
            source.sendFeedback(() -> Text.translatable("manhunt.chat.minimum", Text.translatable("manhunt.role.runner")).formatted(Formatting.RED), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
