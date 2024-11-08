package libreh.manhunt.command.lobby;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import libreh.manhunt.config.gui.ConfigGui;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class ConfigGuiCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("configgui")
                .requires(ServerCommandSource::isExecutedByPlayer)
                .executes(context -> openConfigGui(context.getSource()))
        );
    }

    private static int openConfigGui(ServerCommandSource source) {
        ConfigGui.openConfigGui(source.getPlayer());

        return Command.SINGLE_SUCCESS;
    }
}
