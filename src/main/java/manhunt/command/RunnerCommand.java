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

public class RunnerCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("runner")
                .requires(source -> ManhuntMod.gameState == GameState.PREGAME).executes(context -> selfRunner(context.getSource()))
                .then(CommandManager.argument("targets", EntityArgumentType.players())
                        .requires(source -> source.isExecutedByPlayer() && ManhuntMod.checkLeaderPermission(source.getPlayer(), "manhunt.runner") || !source.isExecutedByPlayer())
                        .executes(context -> setRunners(context.getSource(), EntityArgumentType.getPlayers(context,
                                "targets")))
                )
        );
    }

    private static int selfRunner(ServerCommandSource source) {
        if (source.isExecutedByPlayer() && (ManhuntConfig.CONFIG.getRolePreset() == 1 || ManhuntMod.checkLeaderPermission(source.getPlayer(), "manhunt.runner"))) {
            var player = source.getPlayer();
            var server = source.getServer();
            var scoreboard = server.getScoreboard();
            var runnersTeam = scoreboard.getTeam("runners");
            if (!player.isTeamPlayer(runnersTeam)) {
                scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), runnersTeam);

                server.getPlayerManager().broadcast(Text.translatable("chat.manhunt.joined_team",
                                Text.literal(player.getNameForScoreboard()).formatted(ManhuntConfig.CONFIG.getRunnersColor())
                                ,
                                Text.translatable("role.manhunt.hunters").formatted(ManhuntConfig.CONFIG.getRunnersColor())),
                        false);
            } else {
                source.sendFeedback(() -> Text.translatable("chat.manhunt.already_team", Text.translatable("role" +
                        ".manhunt.hunter")).formatted(Formatting.RED), false);
            }
        } else {
            source.sendFeedback(() -> Text.translatable("command.unknown.command").formatted(Formatting.RED), false);
            source.sendFeedback(() -> Text.translatable("text.manhunt.both",
                    Text.literal("runner").styled(style -> style.withUnderline(true)), Text.translatable("command" +
                            ".context.here").formatted(Formatting.RED)), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int setRunners(ServerCommandSource source, Collection<ServerPlayerEntity> players) {
        for (ServerPlayerEntity player : players) {
            player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(),
                    player.getScoreboard().getTeam("runners"));

            source.getServer().getPlayerManager().broadcast(Text.translatable("chat.manhunt.set_role",
                    Text.literal(player.getNameForScoreboard()).formatted(ManhuntConfig.CONFIG.getRunnersColor()),
                    Text.translatable("role.manhunt.runner").formatted(ManhuntConfig.CONFIG.getRunnersColor())), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
