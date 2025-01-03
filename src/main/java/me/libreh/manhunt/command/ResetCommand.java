package me.libreh.manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import me.libreh.manhunt.game.GameState;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.random.Random;

import static me.libreh.manhunt.utils.Fields.gameState;
import static me.libreh.manhunt.utils.Methods.*;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ResetCommand {
    public static long seed = Random.create().nextLong();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("reset")
                .requires(
                        source -> !isPreGame() && (gameState == GameState.POSTGAME &&
                                source.isExecutedByPlayer() && hasPermission(source.getPlayer(), "manhunt.command.reset") ||
                                !source.isExecutedByPlayer()) || isPlaying() && source.isExecutedByPlayer() &&
                                Permissions.check(source, "manhunt.command.force_reset"))
                .executes(context -> resetCommand(Random.create().nextLong()))
                .then(argument("seed", LongArgumentType.longArg())
                        .executes(context -> resetCommand(LongArgumentType.getLong(context,
                                "seed"))))
        );
    }

    private static int resetCommand(long seed) {
        ResetCommand.seed = seed;

        changeState(GameState.PREGAME);

        return Command.SINGLE_SUCCESS;
    }
}
