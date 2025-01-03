package me.libreh.manhunt.command.gui;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import me.libreh.manhunt.gui.ConfigGui;
import net.minecraft.server.command.ServerCommandSource;

import static me.libreh.manhunt.utils.Methods.hasPermission;
import static net.minecraft.server.command.CommandManager.literal;

public class ConfigCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("config")
                .requires(
                        source -> source.isExecutedByPlayer() &&
                                hasPermission(source.getPlayer(), "manhunt.command.config") || !source.isExecutedByPlayer())
                .executes(context -> {
                    ConfigGui.openConfigGui(context.getSource().getPlayer());

                    return Command.SINGLE_SUCCESS;
                }));
    }
}
