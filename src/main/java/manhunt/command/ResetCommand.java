package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import manhunt.ManhuntMod;
import manhunt.game.GameState;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.random.RandomSeed;

import static manhunt.ManhuntMod.getGameState;
import static manhunt.game.ManhuntGame.resetGame;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ResetCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("reset")
                .requires(source -> source.isExecutedByPlayer() && getGameState() != GameState.PREGAME && ManhuntMod.checkPermission(source.getPlayer(), "manhunt.reset"))
                .executes(context -> resetCommand(context.getSource(), RandomSeed.getSeed()))
                .then(argument("seed", LongArgumentType.longArg())
                        .executes(context -> resetCommand(context.getSource(), LongArgumentType.getLong(context, "seed")))
                )
        );
    }

    private static int resetCommand(ServerCommandSource source, long seed) {
        resetGame(source.getServer(), seed);

        return Command.SINGLE_SUCCESS;
    }
}
