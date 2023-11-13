package manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class PingCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("ping")
                .executes(context -> pingLatency(context.getSource()))
        );
    }

    private static int pingLatency(ServerCommandSource source) {
        source.sendFeedback(() -> Text.translatable("manhunt.ping.ms", source.getPlayer().networkHandler.getLatency()), false);

        return Command.SINGLE_SUCCESS;
    }
}
