package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.ManhuntMod;
import manhunt.config.ManhuntConfig;
import manhunt.game.GameState;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collection;

public class HunterCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("hunter")
                .requires(source -> ManhuntMod.gameState == GameState.PREGAME)
                .executes(context -> selfHunter(context.getSource()))
                .then(CommandManager.argument("targets", EntityArgumentType.players())
                        .requires(source -> source.isExecutedByPlayer() &&
                                ManhuntMod.checkLeaderPermission(source.getPlayer(), "manhunt.hunter") || !source.isExecutedByPlayer())
                        .executes(context -> setHunters(context.getSource(), EntityArgumentType.getPlayers(context, "targets")))
                )
        );
    }

    private static int selfHunter(ServerCommandSource source) {
        if (source.isExecutedByPlayer() && (ManhuntConfig.config.getRolePreset() == 1 || ManhuntMod.checkLeaderPermission(source.getPlayer(), "manhunt.hunter"))) {
            var player = source.getPlayer();
            var server = source.getServer();
            var scoreboard = server.getScoreboard();
            var huntersTeam = scoreboard.getTeam("hunters");
            if (!player.isTeamPlayer(huntersTeam)) {
                scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), huntersTeam);

                server.getPlayerManager().broadcast(Text.translatable("chat.manhunt.joined_team",
                        Text.literal(player.getNameForScoreboard()).formatted(ManhuntConfig.config.getHuntersColor()),
                        Text.translatable("role.manhunt.hunters").formatted(ManhuntConfig.config.getHuntersColor())), false
                );
            } else {
                source.sendFeedback(() -> Text.translatable("chat.manhunt.already_team", Text.translatable("role.manhunt.hunter")).formatted(Formatting.RED), false);
            }
        } else {
            source.sendFeedback(() -> Text.translatable("command.unknown.command").formatted(Formatting.RED), false);
            source.sendFeedback(() -> Text.translatable("text.manhunt.both",
                            Text.literal("hunter").styled(style -> style.withUnderline(true)),
                            Text.translatable("command.context.here").formatted(Formatting.RED)),
                    false
            );
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int setHunters(ServerCommandSource source, Collection<ServerPlayerEntity> players) {
        for (ServerPlayerEntity player : players) {
            player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("hunters"));

            source.getServer().getPlayerManager().broadcast(Text.translatable("chat.manhunt.set_role",
                    Text.literal(player.getNameForScoreboard()).formatted(ManhuntConfig.config.getHuntersColor()),
                    Text.translatable("role.manhunt.hunter").formatted(ManhuntConfig.config.getHuntersColor())),
                    false
            );
        }

        return Command.SINGLE_SUCCESS;
    }
}
