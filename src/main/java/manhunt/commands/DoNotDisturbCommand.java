package manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static manhunt.Manhunt.doNotDisturb;
import static net.minecraft.server.command.CommandManager.literal;

public class DoNotDisturbCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("donotdisturb")
                .then(literal("status")
                        .executes(context -> disturbStatus(context.getSource()))
                )
                .then(literal("on")
                        .executes(context -> disturbOn(context.getSource()))
                )
                .then(literal("off")
                        .executes(context -> disturbOff(context.getSource()))
                )
        );
    }

    private static int disturbStatus(ServerCommandSource source) {
        boolean value = doNotDisturb.get(source.getPlayer().getUuid());

        if (value) {
            source.sendFeedback(() -> Text.translatable("manhunt.get.to", Text.literal("Do not disturb"), Text.literal("on").formatted(Formatting.GRAY)), false);
        } else {
            source.sendFeedback(() -> Text.translatable("manhunt.get.to", Text.literal("Do not disturb"), Text.literal("off").formatted(Formatting.GRAY)), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int disturbOn(ServerCommandSource source) {
        doNotDisturb.put(source.getPlayer().getUuid(), true);

        source.sendFeedback(() -> Text.translatable("manhunt.set.to", Text.literal("Do not disturb"), Text.literal("on").formatted(Formatting.GRAY)), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int disturbOff(ServerCommandSource source) {
        doNotDisturb.put(source.getPlayer().getUuid(), false);

        source.sendFeedback(() -> Text.translatable("manhunt.set.to", Text.literal("Do not disturb"), Text.literal("off").formatted(Formatting.GRAY)), false);

        return Command.SINGLE_SUCCESS;
    }
}
