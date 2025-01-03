package me.libreh.manhunt.command.role;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import me.libreh.manhunt.config.ManhuntConfig;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static me.libreh.manhunt.utils.Fields.SERVER;
import static me.libreh.manhunt.utils.Methods.*;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class OneRunnerCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("onerunner")
                .requires(source -> isPreGame() && source.isExecutedByPlayer() &&
                                hasPermission(source.getPlayer(), "manhunt.one_runner") || !source.isExecutedByPlayer()
                ).then(argument("player", EntityArgumentType.player())
                        .executes(context ->
                                setOneRunner(EntityArgumentType.getPlayer(context, "player")))));
    }

    private static int setOneRunner(ServerPlayerEntity player) {
        for (ServerPlayerEntity serverPlayer : SERVER.getPlayerManager().getPlayerList()) {
            makeHunter(serverPlayer);
        }

        makeRunner(player);

        SERVER.getPlayerManager().broadcast(Text.translatable("chat.manhunt.one_role",
                Text.literal(player.getNameForScoreboard()).formatted(ManhuntConfig.CONFIG.getRunnersColor()),
                Text.translatable("role.manhunt.runner").formatted(ManhuntConfig.CONFIG.getRunnersColor())), false);

        return Command.SINGLE_SUCCESS;
    }
}
