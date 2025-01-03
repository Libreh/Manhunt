package me.libreh.manhunt.command.game;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static me.libreh.manhunt.utils.Methods.isPreGame;
import static me.libreh.manhunt.utils.Methods.resetPlayerHealth;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class FeedCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("feed")
                .then(argument("targets", EntityArgumentType.players())
                        .requires(source ->  (source.isExecutedByPlayer() &&
                                Permissions.check(source, "manhunt.command.feed") ||
                                !source.isExecutedByPlayer()) &&
                                !isPreGame())
                        .executes(context -> {
                            for (ServerPlayerEntity player : EntityArgumentType.getPlayers(context, "targets")) {
                                player.setHealth(player.getMaxHealth());
                                resetPlayerHealth(player);
                            }

                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }

}