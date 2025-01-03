package me.libreh.manhunt.command.role;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collection;

import static me.libreh.manhunt.config.ManhuntConfig.CONFIG;
import static me.libreh.manhunt.utils.Fields.SERVER;
import static me.libreh.manhunt.utils.Methods.*;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RunnerCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("runner")
                .requires(source -> isPreGame())
                .executes(context -> selfRunner(context.getSource()))
                .then(argument("targets", EntityArgumentType.players())
                        .requires(source -> source.isExecutedByPlayer() &&
                                hasPermission(source.getPlayer(), "manhunt.runner") ||
                                !source.isExecutedByPlayer())
                        .executes(context ->
                                setRunners(EntityArgumentType.getPlayers(context, "targets")))));
    }

    private static int selfRunner(ServerCommandSource source) {
        if (source.isExecutedByPlayer() && (CONFIG.getPresetMode().equals("free_select") ||
                hasPermission(source.getPlayer(), "manhunt.runner"))
        ) {
            var player = source.getPlayer();
            if (isHunter(player)) {
                makeRunner(player);

                SERVER.getPlayerManager().broadcast(Text.translatable("chat.manhunt.joined_team",
                        Text.literal(player.getNameForScoreboard()).formatted(CONFIG.getRunnersColor()),
                        Text.translatable("role.manhunt.runners").formatted(CONFIG.getRunnersColor())), false);
            } else {
                source.sendFeedback(() -> Text.translatable("chat.manhunt.already_team",
                        Text.translatable("role.manhunt.runner")).formatted(Formatting.RED), false);
            }
        } else {
            source.sendFeedback(() -> Text.translatable("command.unknown.command").formatted(Formatting.RED), false);
            source.sendFeedback(() -> Text.translatable("text.manhunt.both",
                    Text.literal("runner").styled(style -> style.withUnderline(true)),
                    Text.translatable("command.context.here").formatted(Formatting.RED)), false
            );
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int setRunners(Collection<ServerPlayerEntity> players) {
        for (ServerPlayerEntity player : players) {
            makeRunner(player);

            SERVER.getPlayerManager().broadcast(Text.translatable("chat.manhunt.set_role",
                    Text.literal(player.getNameForScoreboard()).formatted(CONFIG.getRunnersColor()),
                    Text.translatable("role.manhunt.runner").formatted(CONFIG.getRunnersColor())), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
