package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.ManhuntMod;
import manhunt.game.GameState;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.GameMode;

public class SpectatorCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("spectator")
                .requires(source -> ManhuntMod.gameState == GameState.POSTGAME)
                .executes(context -> setSpectator(context.getSource()))
        );
    }

    private static int setSpectator(ServerCommandSource source) {
        source.getPlayer().changeGameMode(GameMode.SPECTATOR);

        return Command.SINGLE_SUCCESS;
    }
}
