package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static manhunt.game.ManhuntGame.gameState;
import static manhunt.game.ManhuntState.PLAYING;
import static manhunt.game.ManhuntState.PREGAME;

public class HunterCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("hunter")
                .executes(context -> setOneselfHunter(context.getSource()))
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> setSomeoneHunter(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                )
        );
    }

    private static int setOneselfHunter(ServerCommandSource source) {
        ServerPlayerEntity sourcePlayer = source.getPlayer();

        if (gameState == PREGAME) {
            sourcePlayer.getScoreboard().clearTeam(sourcePlayer.getName().getString());
            sourcePlayer.getScoreboard().addScoreHolderToTeam(sourcePlayer.getName().getString(), sourcePlayer.getScoreboard().getTeam("hunters"));

            source.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.hunter", Text.literal(sourcePlayer.getName().getString())).formatted(Formatting.RED), false);
        } else if (gameState == PLAYING) {
            source.sendFeedback(() -> Text.translatable("manhunt.chat.playing"), false);
        } else {
            source.sendFeedback(() -> Text.translatable("manhunt.chat.postgame"), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int setSomeoneHunter(ServerCommandSource source, ServerPlayerEntity player) {
        if (gameState == PREGAME) {
            if (source.hasPermissionLevel(1) || source.hasPermissionLevel(2) || source.hasPermissionLevel(3) || source.hasPermissionLevel(4)) {
                player.getScoreboard().clearTeam(player.getName().getString());
                player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("hunters"));

                player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.hunter.set", Text.literal(player.getName().getString())).formatted(Formatting.RED), false);
            } else {
                source.sendFeedback(() -> Text.translatable("manhunt.chat.onlyleader"), false);
            }
        } else if (gameState == PLAYING) {
            source.sendFeedback(() -> Text.translatable("manhunt.chat.playing"), false);
        } else {
            source.sendFeedback(() -> Text.translatable("manhunt.chat.postgame"), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
