package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.game.ManhuntGame;
import manhunt.util.MessageUtil;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class RunnerCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("runner")
                .executes(context -> setRunner(context.getSource(), context.getSource().getPlayer()))
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> setRunner(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                )
        );
    }

    private static int setRunner(ServerCommandSource source, ServerPlayerEntity player) {
        if (source.hasPermissionLevel(2) || source.hasPermissionLevel(4)) {
            ManhuntGame.currentRole.put(player.getUuid(), "runner");

            MessageUtil.sendBroadcast("manhunt.chat.role.runner", player.getName().getString());
        } else {
            source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.player"), false);
        }
        return Command.SINGLE_SUCCESS;
    }
}
