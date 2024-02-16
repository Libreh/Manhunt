package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.game.ManhuntGame;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class SettingsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("settings")
                .executes(context -> showSettingsToSource(context.getSource()))
        );
    }

    private static int showSettingsToSource(ServerCommandSource source) {
        ManhuntGame.showSettings(source.getPlayer());

        return Command.SINGLE_SUCCESS;
    }


}
