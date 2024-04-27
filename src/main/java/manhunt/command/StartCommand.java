package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.ManhuntMod;
import manhunt.game.GameState;
import manhunt.game.ManhuntGame;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.popcraft.chunky.ChunkyProvider;
import org.popcraft.chunky.api.ChunkyAPI;

public class StartCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("start")
                .requires(source -> source.isExecutedByPlayer() && ManhuntMod.getGameState() == GameState.PREGAME && (Permissions.check(source.getPlayer(), "manhunt.start") || (source.hasPermissionLevel(1) || source.hasPermissionLevel(2) || source.hasPermissionLevel(3) || source.hasPermissionLevel(4))))
                .executes(context -> executeStart(context.getSource()))
        );
    }

    private static int executeStart(ServerCommandSource source) {
        if (source.getServer().getScoreboard().getTeam("runners").getPlayerList().isEmpty()) {
            source.sendFeedback(() -> Text.translatable("manhunt.chat.minimum", Text.translatable("manhunt.runner")).formatted(Formatting.RED), false);
        } else if (!source.getServer().getScoreboard().getTeam("runners").getPlayerList().isEmpty()) {
            ManhuntGame.startGame(source.getServer());

            if (ManhuntMod.isChunkyIntegration() && !ManhuntMod.isPreloaded()) {
                ChunkyAPI chunky = ChunkyProvider.get().getApi();

                chunky.cancelTask("manhunt:overworld");
                chunky.cancelTask("manhunt:the_nether");
            }
        }

        return Command.SINGLE_SUCCESS;
    }
}
