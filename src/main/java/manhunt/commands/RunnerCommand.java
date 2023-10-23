package manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static manhunt.Manhunt.currentRole;
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

        source.sendFeedback(() -> Text.translatable("manhunt.chat.role", player.getName().getString(), Text.literal("Runner").formatted(Formatting.GREEN)), true);


        return Command.SINGLE_SUCCESS;
    }
}
