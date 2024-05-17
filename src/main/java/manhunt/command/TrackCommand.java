package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.game.GameState;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static manhunt.ManhuntMod.getGameState;
import static manhunt.game.ManhuntGame.updateCompass;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TrackCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("hunter")
                .requires(source -> getGameState() == GameState.PLAYING)
                .then(argument("player", EntityArgumentType.player())
                        .executes(context -> trackPlayer(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                )
        );
    }

    private static int trackPlayer(ServerCommandSource source, ServerPlayerEntity player) {
        updateCompass(source.getPlayer(), source.getPlayer().getMainHandStack(), player);

        return Command.SINGLE_SUCCESS;
    }
}
