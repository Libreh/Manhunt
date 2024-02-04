package manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.util.MessageUtil;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class PingCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("ping")
                .executes(context -> pingLatency(context.getSource()))
        );
    }

    private static int pingLatency(ServerCommandSource source) {
        source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.ping.ms", source.getPlayer().networkHandler.getLatency()), false);

        return Command.SINGLE_SUCCESS;
    }
}
