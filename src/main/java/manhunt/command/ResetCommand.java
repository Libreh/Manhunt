package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import manhunt.ManhuntMod;
import manhunt.game.GameState;
import manhunt.game.ManhuntGame;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.random.RandomSeed;

public class ResetCommand {
    public static long seed;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("reset")
                .requires(source -> ManhuntMod.gameState != GameState.PREGAME && (ManhuntMod.gameState == GameState.POSTGAME &&
                        source.isExecutedByPlayer() && ManhuntMod.checkLeaderPermission(source.getPlayer(), "manhunt.reset") || !source.isExecutedByPlayer()) ||
                        ManhuntMod.gameState == GameState.PLAYING && source.isExecutedByPlayer() && Permissions.check(source.getPlayer(), "manhunt.force_reset"))
                .executes(context -> resetCommand(context.getSource(), RandomSeed.getSeed()))
                .then(CommandManager.argument("seed", LongArgumentType.longArg())
                        .executes(context -> resetCommand(context.getSource(), LongArgumentType.getLong(context, "seed")))
                )
        );
    }

    private static int resetCommand(ServerCommandSource source, long seed) {
        ResetCommand.seed = seed;
        ManhuntGame.reset(source.getServer());

        return Command.SINGLE_SUCCESS;
    }
}
