package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PingCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("ping")
                .executes(context -> selfPing(context.getSource()))
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> playerPing(context.getSource(), EntityArgumentType.getPlayer(context,
                                "player")))
                )
        );
    }

    private static int selfPing(ServerCommandSource source) {
        if (source.isExecutedByPlayer()) {
            source.sendFeedback(() -> Text.translatable("chat.manhunt.ping",
                    Text.literal(String.valueOf(source.getPlayer().networkHandler.getLatency()))), false);
        } else {
            source.sendFeedback(() -> Text.translatable("command.unknown.command").formatted(Formatting.RED), false);
            source.sendFeedback(() -> Text.translatable("text.manhunt.both",
                    Text.literal("ping").styled(style -> style.withUnderline(true)), Text.translatable("command" +
                            ".context.here").formatted(Formatting.RED)), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int playerPing(ServerCommandSource source, ServerPlayerEntity player) {
        source.sendFeedback(() -> Text.translatable("chat.manhunt.ping",
                Text.literal(String.valueOf(player.networkHandler.getLatency()))), false);

        return Command.SINGLE_SUCCESS;
    }
}