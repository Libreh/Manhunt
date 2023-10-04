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
                .executes(context -> doNotDisturb(context.getSource()))
        );
    }

    private static int doNotDisturb(ServerCommandSource source) {
        boolean bool;
        if (getPlayerData(source.getPlayer()).getString("doNotDisturb") == null) {
            getPlayerData(source.getPlayer()).put("doNotDisturb", true);
            bool = true;
        } else if (getPlayerData(source.getPlayer()).getString("doNotDisturb") != null) {
            if (getPlayerData(source.getPlayer()).getString("doNotDisturb").equals(false)) {
                getPlayerData(source.getPlayer()).put("doNotDisturb", true);
                bool = true;
            } else if (getPlayerData(source.getPlayer()).getString("doNotDisturb").equals(true)) {
                getPlayerData(source.getPlayer()).put("doNotDisturb", false);
                bool = false;
            } else {
                bool = true;
            }
        } else {
            bool = true;
        }

        source.sendFeedback(() -> Text.translatable("manhunt.donotdisturb.set", bool), false);

        return Command.SINGLE_SUCCESS;
    }
}
