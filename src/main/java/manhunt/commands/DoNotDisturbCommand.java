package manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.Manhunt;
import manhunt.util.MessageUtil;
import net.minecraft.server.command.ServerCommandSource;

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
        Manhunt.table.beginTransaction();

        boolean value = Manhunt.table.get(source.getPlayer().getUuid()).getBool("donotdisturb");

        if (value) {
            source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.get.to", "Do Not Disturb", "On"), false);
        } else {
            source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.get.to", "Do Not Disturb", "Off"), false);
        }

        Manhunt.table.endTransaction();

        return Command.SINGLE_SUCCESS;
    }

    private static int disturbOn(ServerCommandSource source) {
        Manhunt.table.beginTransaction();

        Manhunt.table.get(source.getPlayer().getUuid()).put("donotdisturb", true);

        Manhunt.table.endTransaction();

        source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.set.to", "Do Not Disturb", "On"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int disturbOff(ServerCommandSource source) {
        Manhunt.table.beginTransaction();

        Manhunt.table.get(source.getPlayer().getUuid()).put("donotdisturb", false);

        Manhunt.table.endTransaction();

        source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.set.to", "Do Not Disturb", "Off"), false);

        return Command.SINGLE_SUCCESS;
    }
}
