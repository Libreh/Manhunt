package manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static manhunt.Manhunt.getPlayerScore;
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
        int value = getPlayerScore(source.getPlayer(), "doNotDisturb").getScore();

        if (value == 1) {
            source.sendFeedback(() -> Text.translatable("manhunt.get.to", Text.literal("Do not disturb"), Text.literal("on").formatted(Formatting.GRAY)), false);
        } else {
            source.sendFeedback(() -> Text.translatable("manhunt.get.to", Text.literal("Do not disturb"), Text.literal("off").formatted(Formatting.GRAY)), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int disturbOn(ServerCommandSource source) {
        getPlayerScore(source.getPlayer(), "doNotDisturb").setScore(1);

        source.sendFeedback(() -> Text.translatable("manhunt.set.to", Text.literal("Do not disturb"), Text.literal("on").formatted(Formatting.GRAY)), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int disturbOff(ServerCommandSource source) {
        getPlayerScore(source.getPlayer(), "doNotDisturb").setScore(0);

        source.sendFeedback(() -> Text.translatable("manhunt.set.to", Text.literal("Do not disturb"), Text.literal("off").formatted(Formatting.GRAY)), false);

        return Command.SINGLE_SUCCESS;
    }
}
