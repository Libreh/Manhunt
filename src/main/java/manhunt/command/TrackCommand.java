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
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TrackCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("hunter")
                .requires(source -> source.isExecutedByPlayer() && ManhuntMod.gameState == GameState.PLAYING)
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> trackPlayer(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                )
        );
    }

    private static int trackPlayer(ServerCommandSource source, ServerPlayerEntity player) {
        if (player.isTeamPlayer(source.getPlayer().getScoreboardTeam())) {
            ManhuntGame.updateCompass(source.getPlayer(), source.getPlayer().getMainHandStack(), player);
        } else {
            source.sendFeedback(() -> Text.translatable("chat.manhunt.track_team").formatted(Formatting.RED), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
