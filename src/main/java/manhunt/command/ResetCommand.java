package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import manhunt.game.GameState;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.random.RandomSeed;

import static manhunt.ManhuntMod.checkPermission;
import static manhunt.ManhuntMod.state;
import static manhunt.game.ManhuntGame.gameReset;

public class ResetCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("reset")
                .requires(source -> source.isExecutedByPlayer() && state != GameState.PREGAME && checkPermission(source.getPlayer(), "manhunt.reset"))
                .executes(context -> resetCommand(context.getSource(), RandomSeed.getSeed()))
                .then(CommandManager.argument("seed", LongArgumentType.longArg())
                        .executes(context -> resetCommand(context.getSource(), LongArgumentType.getLong(context, "seed")))
                )
        );
    }

    private static int resetCommand(ServerCommandSource source, long seed) {
        gameReset(source.getServer(), seed);

        return Command.SINGLE_SUCCESS;
    }
}
