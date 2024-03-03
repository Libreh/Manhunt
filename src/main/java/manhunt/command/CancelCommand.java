package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static manhunt.config.ManhuntConfig.AUTO_RESET;

public class CancelCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("ping")
                .executes(context -> runCancelleation(context.getSource()))
        );
    }

    private static int runCancelleation(ServerCommandSource source) {
        if (source.hasPermissionLevel(1) || source.hasPermissionLevel(2) || source.hasPermissionLevel(3) || source.hasPermissionLevel(4)) {
            AUTO_RESET.set(false);

            source.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.cancel"), false);
        } else {
            source.sendFeedback(() -> Text.translatable("manhunt.chat.onlyleader").formatted(Formatting.RED), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
