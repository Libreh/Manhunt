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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.popcraft.chunky.ChunkyProvider;
import org.popcraft.chunky.api.ChunkyAPI;

import static manhunt.ManhuntMod.*;

public class StartCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("start")
                .requires(source -> ManhuntMod.gameState == GameState.PREGAME && GameEvents.startReset && source.isExecutedByPlayer() && ManhuntMod.checkLeaderPermission(source.getPlayer(), "manhunt.start") || !source.isExecutedByPlayer())
                .executes(context -> executeStart(context.getSource()))
        );
    }

    private static int executeStart(ServerCommandSource source) {
        var server = source.getServer();
        var runnersTeam = server.getScoreboard().getTeam("runners");

        int runners = 0;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player.isTeamPlayer(runnersTeam)) {
                runners++;
                break;
            }
        }
        if (runners != 0) {
            if (ManhuntGame.chunkyLoaded && ManhuntConfig.CONFIG.isChunky()) {
                ChunkyAPI chunky = ChunkyProvider.get().getApi();

                chunky.cancelTask(String.valueOf(overworld.getRegistryKey().getValue()));
                chunky.cancelTask(String.valueOf(theNether.getRegistryKey().getValue()));
                chunky.cancelTask(String.valueOf(theEnd.getRegistryKey().getValue()));
            }

            GameEvents.startingTime = 120;
            GameEvents.starting = true;
        } else {
            source.sendFeedback(() -> Text.translatable("chat.manhunt.minimum", Text.translatable("role.manhunt" +
                    ".runner")).formatted(Formatting.RED), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
