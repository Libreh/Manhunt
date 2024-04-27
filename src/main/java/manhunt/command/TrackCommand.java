package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.ManhuntMod;
import manhunt.game.GameState;
import manhunt.game.ManhuntGame;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class TrackCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("hunter")
                .requires(source -> ManhuntMod.getGameState() == GameState.PLAYING)
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> trackPlayer(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                )
        );
    }

    private static int trackPlayer(ServerCommandSource source, ServerPlayerEntity player) {
        ManhuntGame.updateCompass(source.getPlayer(), source.getPlayer().getMainHandStack().getNbt(), player);

        return Command.SINGLE_SUCCESS;
    }
}
