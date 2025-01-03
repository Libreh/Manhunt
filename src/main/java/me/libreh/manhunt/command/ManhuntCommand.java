package me.libreh.manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

import static me.libreh.manhunt.config.ManhuntConfig.CONFIG;
import static me.libreh.manhunt.utils.Methods.hasPermission;
import static net.minecraft.server.command.CommandManager.literal;

public class ManhuntCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("manhunt")
                .then(literal("reload")
                        .requires(source -> (source.isExecutedByPlayer() &&
                                hasPermission(source.getPlayer(), "manhunt.command.reload")) || (!source.isExecutedByPlayer())
                        )
                        .executes(context -> {
                            CONFIG.load();

                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }
}

