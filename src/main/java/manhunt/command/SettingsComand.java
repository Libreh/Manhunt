package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.game.ManhuntSettings;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class SettingsComand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("settings")
                .requires(ServerCommandSource::isExecutedByPlayer)
                .executes(context -> showSettings(context.getSource()))
        );
    }

    private static int showSettings(ServerCommandSource source) {
        ManhuntSettings.openSettingsGui(source.getPlayer());

        return Command.SINGLE_SUCCESS;
    }
}
