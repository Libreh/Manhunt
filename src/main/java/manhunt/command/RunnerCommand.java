package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.game.GameState;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collection;

import static manhunt.ManhuntMod.*;

public class RunnerCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("runner")
                .requires(source -> source.isExecutedByPlayer() && state == GameState.PREGAME && config.getTeamPreset() == 1)
                .executes(context -> setRunner(context.getSource(), context.getSource().getPlayer()))
                .then(CommandManager.argument("targets", EntityArgumentType.players())
                        .requires(source -> checkPermission(source.getPlayer(), "manhunt.runner"))
                        .executes(context -> setRunners(context.getSource(), EntityArgumentType.getPlayers(context, "targets")))
                )
        );
    }

    private static int setRunner(ServerCommandSource source, ServerPlayerEntity player) {
        if (!player.getScoreboard().getTeam("runners").getPlayerList().contains(player.getNameForScoreboard())) {
            player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("runners"));

            player.getServer().getPlayerManager().broadcast(Text.translatable("chat.joined_team", Text.literal(player.getNameForScoreboard()).formatted(config.getRunnersColor()), Text.translatable("role.manhunt.runners").formatted(config.getRunnersColor())), false);
        } else {
            source.sendFeedback(() -> Text.translatable("chat.already_team", Text.translatable("role.manhunt.runner")).formatted(Formatting.RED), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int setRunners(ServerCommandSource source, Collection<ServerPlayerEntity> players) {
        for (ServerPlayerEntity player : players) {
            player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("runners"));

            player.getServer().getPlayerManager().broadcast(Text.translatable("chat.set_role", Text.literal(player.getNameForScoreboard()).formatted(config.getRunnersColor()), Text.translatable("role.manhunt.runner").formatted(config.getRunnersColor())), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
