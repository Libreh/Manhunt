package manhunt.command.lobby;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.ManhuntMod;
import manhunt.config.ManhuntConfig;
import manhunt.event.OnGameTick;
import manhunt.game.GameState;
import manhunt.game.ManhuntGame;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Unit;
import net.minecraft.util.math.ChunkPos;

import static manhunt.ManhuntMod.getOverworld;

public class StartCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("start")
                .requires(source -> ManhuntMod.gameState == GameState.PREGAME && OnGameTick.startReset && source.isExecutedByPlayer() && ManhuntMod.checkLeaderPermission(source.getPlayer(), "manhunt.start") || !source.isExecutedByPlayer())
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
                server.getCommandManager().executeWithPrefix(server.getCommandSource(), "chunky cancel");
                server.getCommandManager().executeWithPrefix(server.getCommandSource(), "chunky confirm");
            }

            OnGameTick.startingTime = 120;
            OnGameTick.starting = true;
            getOverworld().getChunkManager().addTicket(ChunkTicketType.START,
                    ChunkPos.fromRegion(ManhuntGame.worldSpawnPos.getX(),
                            ManhuntGame.worldSpawnPos.getZ()), 8, Unit.INSTANCE);
        } else {
            source.sendFeedback(() -> Text.translatable("chat.manhunt.minimum", Text.translatable("role.manhunt" +
                    ".runner")).formatted(Formatting.RED), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
