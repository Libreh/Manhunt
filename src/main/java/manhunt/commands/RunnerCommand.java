package manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.util.MessageUtil;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static manhunt.game.ManhuntGame.currentRole;
import static net.minecraft.server.command.CommandManager.literal;

public class RunnerCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("runner")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> setRunner(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                )
        );
    }

    private static int setRunner(ServerCommandSource source, ServerPlayerEntity player) {
        currentRole.put(player.getUuid(), "runner");

        source.sendFeedback(() -> MessageUtil.ofVomponent(player, "manhunt.chat.role.runner", player.getName().getString()), false);

        return Command.SINGLE_SUCCESS;
    }
}
