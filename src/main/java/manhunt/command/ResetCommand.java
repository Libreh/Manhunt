package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.game.GameState;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;

import static manhunt.ManhuntMod.getGameState;
import static manhunt.game.ManhuntGame.resetGame;
import static net.minecraft.server.command.CommandManager.literal;

public class ResetCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("reset")
                .requires(source -> source.isExecutedByPlayer() && getGameState() != GameState.PREGAME && (Permissions.check(source.getPlayer(), "manhunt.reset") || (source.hasPermissionLevel(1) || source.hasPermissionLevel(2) || source.hasPermissionLevel(3) || source.hasPermissionLevel(4))))
                .executes(context -> resetCommand(context.getSource()))
        );
    }

    private static int resetCommand(ServerCommandSource source) {
        resetGame(source.getServer());

        return Command.SINGLE_SUCCESS;
    }
}
