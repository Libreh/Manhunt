package libreh.manhunt.command.lobby;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import libreh.manhunt.config.gui.SettingsGui;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class SettingsGuiCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("settingsgui")
                .requires(ServerCommandSource::isExecutedByPlayer)
                .executes(context -> openSettingsGui(context.getSource()))
        );
    }

    private static int openSettingsGui(ServerCommandSource source) {
        SettingsGui.openSettingsGui(source.getPlayer());

        return Command.SINGLE_SUCCESS;
    }
}
