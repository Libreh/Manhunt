package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.game.ManhuntSettings;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class PreferencesCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("preferences")
                        .requires(ServerCommandSource::isExecutedByPlayer)
                .executes(context -> showPreferences(context.getSource()))
        );
    }

    private static int showPreferences(ServerCommandSource source) {
        ManhuntSettings.openPreferencesGui(source.getPlayer());

        return Command.SINGLE_SUCCESS;
    }
}
