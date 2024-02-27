package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.util.MessageUtil;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

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
        if (gameState == PREGAME) {
            ServerPlayerEntity player = source.getPlayer();

            player.getScoreboard().clearTeam(player.getName().getString());
            player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("hunters"));

            MessageUtil.sendBroadcast("manhunt.chat.role.hunter", player.getName().getString());
        } else if (gameState == PLAYING) {
            source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.playing"), false);
        } else {
            source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.postgame"), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int setSomeoneHunter(ServerCommandSource source, ServerPlayerEntity player) {
        if (gameState == PREGAME) {
            if (source.hasPermissionLevel(1) || source.hasPermissionLevel(2) || source.hasPermissionLevel(3) || source.hasPermissionLevel(4)) {
                player.getScoreboard().clearTeam(player.getName().getString());
                player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("hunters"));

                MessageUtil.sendBroadcast("manhunt.chat.role.hunter", player.getName().getString());
            } else {
                source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.leader"), false);
            }
        } else if (gameState == PLAYING) {
            source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.playing"), false);
        } else {
            source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.postgame"), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
