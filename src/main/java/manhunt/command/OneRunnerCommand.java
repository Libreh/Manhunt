package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.game.ManhuntGame;
import manhunt.util.MessageUtil;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class OneRunnerCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("onerunner")
                .executes(context -> setOneRunnerAndAllHunters(context.getSource(), context.getSource().getPlayer()))
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> setOneRunnerAndAllHunters(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                )
        );
    }

    private static int setOneRunnerAndAllHunters(ServerCommandSource source, ServerPlayerEntity player) {
        if (source.hasPermissionLevel(1) || source.hasPermissionLevel(2) || source.hasPermissionLevel(3) || source.hasPermissionLevel(4)) {
            ManhuntGame.currentRole.put(player.getUuid(), "runner");

            for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
                if (serverPlayer.getUuid() != player.getUuid()) {
                    ManhuntGame.currentRole.put(serverPlayer.getUuid(), "hunter");
                }
            }

            MessageUtil.sendBroadcast("manhunt.chat.role.onerunner", player.getName().getString());
        } else {
            source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.leader"), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
