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

public class HunterCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("hunter")
                .requires(source -> isPreGame())
                .executes(context -> selfHunter(context.getSource()))
                .then(argument("targets", EntityArgumentType.players())
                        .requires(
                                source -> source.isExecutedByPlayer() &&
                                        hasPermission(source.getPlayer(), "manhunt.hunter") ||
                                        !source.isExecutedByPlayer())
                        .executes(context -> setHunters(context.getSource(),
                                EntityArgumentType.getPlayers(context, "targets")))));
    }

    private static int selfHunter(ServerCommandSource source) {
        if (source.isExecutedByPlayer() && (CONFIG.getPresetMode().equals("free_select") ||
                hasPermission(source.getPlayer(), "manhunt.hunter"))
        ) {
            var player = source.getPlayer();

            if (isRunner(player)) {
                makeHunter(player);

                SERVER.getPlayerManager().broadcast(Text.translatable("chat.manhunt.joined_team",
                        Text.literal(player.getNameForScoreboard()).formatted(CONFIG.getHuntersColor()),
                        Text.translatable("role.manhunt.hunters").formatted(CONFIG.getHuntersColor())),false);
            } else {
                source.sendFeedback(() -> Text.translatable("chat.manhunt.already_team", Text.translatable("role.manhunt.hunter")).formatted(Formatting.RED), false);
            }
        } else {
            source.sendFeedback(() -> Text.translatable("command.unknown.command").formatted(Formatting.RED), false);
            source.sendFeedback(() -> Text.translatable("text.manhunt.both",
                    Text.literal("hunter").styled(style -> style.withUnderline(true)),
                    Text.translatable("command.context.here").formatted(Formatting.RED)), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int setHunters(ServerCommandSource source, Collection<ServerPlayerEntity> players) {
        for (ServerPlayerEntity player : players) {
            makeHunter(player);

            source.getServer().getPlayerManager().broadcast(Text.translatable("chat.manhunt.set_role",
                    Text.literal(player.getNameForScoreboard()).formatted(CONFIG.getHuntersColor()),
                    Text.translatable("role.manhunt.hunter").formatted(CONFIG.getHuntersColor())), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
