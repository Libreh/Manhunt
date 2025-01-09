package me.libreh.manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import me.libreh.manhunt.gui.ConfigGui;
import me.libreh.manhunt.gui.PreferencesGui;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class GuiCommands {
    public static void configCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("config")
                .requires(source -> source.isExecutedByPlayer() && Permissions.check(source, "manhunt.config.show", true))
                .executes(context -> {
                    ConfigGui.openConfigGui(context.getSource().getPlayer());

                    return Command.SINGLE_SUCCESS;
                }));
    }

    public static void preferencesCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("preferences")
                .requires(source -> source.isExecutedByPlayer() && Permissions.check(source, "manhunt.preferences", true))
                .executes(context -> {
                    PreferencesGui.openPreferencesGui(context.getSource().getPlayer());

                    return Command.SINGLE_SUCCESS;
                }));
    }
}
