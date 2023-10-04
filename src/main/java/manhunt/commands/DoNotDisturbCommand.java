package manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static manhunt.Manhunt.getPlayerData;
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
        boolean bool = getPlayerData(source.getPlayer()).getBool("doNotDisturb");

        if (bool) {
            source.sendFeedback(() -> Text.translatable("manhunt.donotdisturb.get", Text.translatable("on")), false);
        } else if (!bool) {
            source.sendFeedback(() -> Text.translatable("manhunt.donotdisturb.get", Text.translatable("off")), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int disturbOn(ServerCommandSource source) {
        getPlayerData(source.getPlayer()).put("doNotDisturb", true);

        source.sendFeedback(() -> Text.translatable("manhunt.donotdisturb.get", Text.translatable("on")), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int disturbOff(ServerCommandSource source) {
        getPlayerData(source.getPlayer()).put("doNotDisturb", false);

        source.sendFeedback(() -> Text.translatable("manhunt.donotdisturb.set", Text.translatable("off")), false);

        return Command.SINGLE_SUCCESS;
    }
}
