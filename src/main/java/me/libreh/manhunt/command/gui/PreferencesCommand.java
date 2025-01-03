package me.libreh.manhunt.command.gui;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import me.libreh.manhunt.gui.PreferencesGui;
import net.minecraft.server.command.ServerCommandSource;

import static me.libreh.manhunt.utils.Methods.hasPermission;
import static net.minecraft.server.command.CommandManager.literal;

public class PreferencesCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("preferences")
                .requires(
                        source -> source.isExecutedByPlayer() &&
                                hasPermission(source.getPlayer(), "manhunt.command.preferences") || !source.isExecutedByPlayer())
                .executes(context -> {
                    PreferencesGui.openPreferencesGui(context.getSource().getPlayer());

                    return Command.SINGLE_SUCCESS;
                }));
    }
}
